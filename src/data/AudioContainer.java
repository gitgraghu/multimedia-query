package data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import data.Complex;
import utils.Constants;
import utils.FFT;
import utils.Helper;

public class AudioContainer {
	
	private AudioInputStream audiostream;
	private File audioFile;
	private double samplesleft[];
	private double samplesright[];
	private long framelength;
	private int framesize;
	private long framerate;
	
	public AudioContainer(String audiofilename, boolean isoffline, boolean queryvid){
		
		audioFile = new File(audiofilename);
		
		try {
			audiostream = AudioSystem.getAudioInputStream(audioFile);
			framelength = audiostream.getFrameLength();
			framesize = audiostream.getFormat().getFrameSize();
			framerate = (long) audiostream.getFormat().getFrameRate();
			
			if(isoffline || queryvid){
				GenerateSamples();
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public AudioInputStream getAudioStream(){
		return audiostream;
	}
	
	public void setAudioStream(AudioInputStream audiostream){
		this.audiostream = audiostream;
	}
	
	public void GenerateSamples(){
		byte[] audiobytes = new byte[(int)framelength*framesize];
		
		AudioInputStream audiostream = null;
		try {
			audiostream = AudioSystem.getAudioInputStream(audioFile);
			audiostream.read(audiobytes);
			audiostream.close();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		samplesleft = new double[(int) framelength];
		samplesright = new double[(int) framelength];
		
		int index = 0, i, low,high,sample;
		for (i = 0; i < audiobytes.length; i+=framesize)
		{
			low  = (int)audiobytes[i];
			high = (int)audiobytes[i+1];
			sample = (high << 8) + (low & 0x00ff);
			samplesleft[index] = ((double)sample/Constants.MAX_SAMPLE_VALUE);
			
			low  = (int)audiobytes[i+2];
			high = (int)audiobytes[i+3];
			sample = (high << 8) + (low & 0x00ff);
			samplesright[index] = ((double)sample/Constants.MAX_SAMPLE_VALUE);
			
			index++;
		}
		
	}
	
	   public List<AudioKeyFrame> GenerateAudioKeyFrames(){
       	
       	List<AudioKeyFrame> audiokeyframes= new ArrayList<AudioKeyFrame>();
//      int numchunks = (int) (framelength/(framerate*Constants.AUDIO_CHUNK_TIME));
//      int chunksize = (int) (framerate*Constants.AUDIO_CHUNK_TIME);
       	
       	int chunksize = (int) (Constants.FFT_WINDOW_SIZE*Constants.NUM_FFT_WINDOWS);
       	int numchunks = (int) (framelength/chunksize);
       	
       	int n, i;
       	for(n = 0; n < numchunks; n++){
       		
       		AudioKeyFrame audiokeyframe = new AudioKeyFrame();
       		audiokeyframe.index = (int) (((n*chunksize)/framerate)/(Constants.FRAME_DELAY));
       		System.out.println(audiokeyframe.index);

       		double[] spectrum = new double[Constants.FFT_WINDOW_SIZE/2];
       		
       	
       		for(i=(n*chunksize); i<((n+1)*chunksize); i+=Constants.FFT_WINDOW_SIZE){
       		  
       		  Complex x[] = new Complex[Constants.FFT_WINDOW_SIZE];
       		  for (int j = 0; j < Constants.FFT_WINDOW_SIZE; j++) {
       	            x[j] = new Complex(samplesleft[i], 0);
       		  }
       		  
       		  Complex[] yleft = FFT.fft(x);
       		  
       		  for (int j = 0; j < Constants.FFT_WINDOW_SIZE; j++) {
       	            x[j] = new Complex(samplesright[i], 0);
       		  }
       		  
       		  Complex[] yright = FFT.fft(x);
       		  
       		  for (int k=0; k < (Constants.FFT_WINDOW_SIZE/2); k++){
        		     spectrum[k] =  spectrum[k] + ((yleft[k].abs() + yright[k].abs())/2.0);
        	  }
       	   }
       	
 		    for (int l=0; l < (Constants.FFT_WINDOW_SIZE/2); l++){
 		      spectrum[l] =  ((double)spectrum[l]/Constants.NUM_FFT_WINDOWS);
 		    }
 		    
 		    audiokeyframe.setSpectrum(spectrum);
 		    audiokeyframes.add(audiokeyframe);
       	}

       	return audiokeyframes;
       	
       }
	   
		private double CalculateDirectedDivergence(AudioKeyFrame previous, AudioKeyFrame current){
			
			double directeddivergence = Helper.CalculateEulersDistance(previous.spectrum, current.spectrum);
			
			return directeddivergence;
		}
	   
	   public Object[] SequenceMatch(HashMap<String,List<AudioKeyFrame>> audiofeatures){
			
			int i,j;
			AudioKeyFrame q,r;
			HashMap<String,Score> audioscores = new HashMap<String,Score>();
			HashMap<String, Double> audiomatch = new HashMap<String, Double>();
			double divergence = 0.0;
			double mindivergence = 100;
			double maxdivergence = 0.0;
			double totaldivergence = 0.0;
			int minvidkeyframe = -1;
			List<AudioKeyFrame> querykeyframes = GenerateAudioKeyFrames();
			for(String videoname : audiofeatures.keySet()){			
				List<AudioKeyFrame> audiokeyframes = audiofeatures.get(videoname);
				Score audioscore = new Score();
				maxdivergence = -1;
				totaldivergence = 0;
				for(i=0; i<querykeyframes.size(); i++){
					
					q = querykeyframes.get(i);
					
					mindivergence = 100;
					minvidkeyframe = -1;
					for(j=0; j<audiokeyframes.size(); j++){
						r = audiokeyframes.get(j);
						divergence = CalculateDirectedDivergence(q, r);
						if(divergence < mindivergence){
							mindivergence = divergence;
							minvidkeyframe = r.getIndex();
						}
					}

					int pos = Collections.binarySearch(audioscore.framenumbers, minvidkeyframe);
				    if (pos < 0) {	
				    	audioscore.framenumbers.add((-pos)-1, minvidkeyframe);
				    	audioscore.score.add((-pos)-1, mindivergence);
				    }
				    else{
				    	double sc = audioscore.score.get(pos);
				    	if(mindivergence < sc){
					    	audioscore.framenumbers.set(pos,minvidkeyframe);
					    	audioscore.score.set(pos,mindivergence);
				    	}
				    }
				    totaldivergence+=mindivergence;
					if(mindivergence > maxdivergence){
			    		maxdivergence=mindivergence;	
			    	}
				}
				
				audioscores.put(videoname, audioscore);
				audiomatch.put(videoname, maxdivergence);
			}
			
			Object ret[] = new Object[2];
			ret[0] = audiomatch;
			ret[1] = audioscores;

			return ret;
		}
	   
	   public Score ReverseMatch(HashMap<String,List<AudioKeyFrame>> audiofeatures, String videokey){
			
			int i,j;
			AudioKeyFrame q,r;

			double divergence = 0.0;
			double mindivergence = 100;
			int minvidkeyframe = -1;
			
			List<AudioKeyFrame> querykeyframes = GenerateAudioKeyFrames();
			List<AudioKeyFrame> videokeyframes = audiofeatures.get(videokey);
			Score audioscore = new Score();
			
				for(i=0; i<videokeyframes.size(); i++){
					
					q = videokeyframes.get(i);
					
					mindivergence = 100;
					minvidkeyframe = -1;
					for(j=0; j<querykeyframes.size(); j++){
						r = querykeyframes.get(j);
						divergence = CalculateDirectedDivergence(q, r);
						if(divergence < mindivergence){
							mindivergence = divergence;
							minvidkeyframe = q.getIndex();
						}
					}
						audioscore.framenumbers.add(minvidkeyframe);
						audioscore.score.add(mindivergence);
				    }
				  
			return audioscore;
		}

	  
}