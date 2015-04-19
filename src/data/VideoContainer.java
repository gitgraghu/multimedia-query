package data;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import utils.Constants;
import utils.Helper;

public class VideoContainer {
	
	private int numframes;
	public ImageContainer videoframes[];
	public AudioContainer audiotrack;
	
	public VideoContainer(String videopath, int framecount, boolean isoffline, boolean generatehistograms){
		videoframes = new ImageContainer[framecount];
		numframes 	= framecount;
		LoadVideo(videopath, isoffline, generatehistograms);
	}
	
	public void LoadVideo(String videopath, boolean isoffline, boolean queryvid) {
		
		int i;
		for(i = 0; i < numframes; i++){
			String suffix = String.format("%03d.rgb", i + 1);
			videoframes[i] = new ImageContainer(videopath + suffix);
			
			if(!isoffline){
			videoframes[i].DrawImage();
			}
			
			if(queryvid){
			videoframes[i].GenerateHistogramDistributions();	
			}
		}
		
		LoadAudio(videopath, isoffline, queryvid);
	}

	
	public void LoadAudio(String videokey, boolean isoffline, boolean queryvid){
		audiotrack = new AudioContainer(videokey + ".wav", isoffline, queryvid);
	}
	
	public int getFrameCount(){
		return numframes;
	}
	
	private double CalculateDirectedDivergence(VideoKeyFrame previous, VideoKeyFrame current){
		
		double directeddivergence = Helper.CalculateDirectedDivergence(previous.pdfY, current.pdfY) +
									Helper.CalculateDirectedDivergence(previous.pdfU, current.pdfU) +
									Helper.CalculateDirectedDivergence(previous.pdfV, current.pdfV);
		
		return directeddivergence;
	}
	
	public List<VideoKeyFrame> FindKeyFrames(){
		List<VideoKeyFrame> keyframes = new ArrayList<VideoKeyFrame>();
		ImageContainer p,q = null,prev;
		double divergence, csum = 0.0;
		
		p = videoframes[0];
		
		VideoKeyFrame k = new VideoKeyFrame();
		k.setIndex(0);
		k.setPdfs(p.pdfY, p.pdfU, p.pdfV);
		keyframes.add(k);
		
		
		int i;
		for(i = 1; i < numframes; i++){
			
			q = videoframes[i];
			divergence = CalculateDirectedDivergence(p, q);
			csum = csum + divergence;
			
			prev = videoframes[i-1];
			divergence = CalculateDirectedDivergence(prev, q);
			
			if(csum > Constants.CUMULATIVE_TRESHOLD || divergence > Constants.DIVERGENCE_TRESHOLD){
				csum = 0.0;
				k = new VideoKeyFrame();
				k.setIndex(i);
				k.setPdfs(q.pdfY, q.pdfU, q.pdfV);
				keyframes.add(k);
				p = videoframes[i];
			}
		}
		
		return keyframes;
	}
	
	public Object[] SequenceMatch(HashMap<String,List<VideoKeyFrame>> videofeatures){
		
		int i,j;
		VideoKeyFrame q,r;
		HashMap<String,Score> videoscores = new HashMap<String,Score>();
		HashMap<String, Double> videomatch = new HashMap<String, Double>();
		double divergence = 0.0;
		double mindivergence = 100;
		double maxdivergence = 0.0;
		int minvidkeyframe = -1;
		List<VideoKeyFrame> querykeyframes = FindKeyFrames();
		for(String videoname : videofeatures.keySet()){
			
			List<VideoKeyFrame> videokeyframes = videofeatures.get(videoname);
			Score videoscore = new Score();
			maxdivergence = -1;
			for(i=0; i<querykeyframes.size(); i++){
				
				q = querykeyframes.get(i);
				
				mindivergence = 100;
				minvidkeyframe = -1;
				for(j=0; j<videokeyframes.size(); j++){
					r = videokeyframes.get(j);
					divergence = CalculateDirectedDivergence(q, r);
					if(divergence < mindivergence){
						mindivergence = divergence;
						minvidkeyframe = r.getIndex();
					}
				}

				int pos = Collections.binarySearch(videoscore.framenumbers, minvidkeyframe);
			    if (pos < 0) {	
			    	videoscore.framenumbers.add((-pos)-1, minvidkeyframe);
			    	videoscore.score.add((-pos)-1, mindivergence);
			    }
			    else{
			    	double sc = videoscore.score.get(pos);
			    	if(mindivergence < sc){
				    	videoscore.framenumbers.set(pos,minvidkeyframe);
				    	videoscore.score.set(pos,mindivergence);
			    	}
			    }
			    	if(mindivergence > maxdivergence){
			    		maxdivergence=mindivergence;	
			    	}
			}
			
			videoscores.put(videoname, videoscore);
			videomatch.put(videoname, maxdivergence);
				
		}
		
		Object ret[] = new Object[2];
		ret[0] = videomatch;
		ret[1] = videoscores;
		return ret;
	}
	
public Score ReverseMatch(HashMap<String,List<VideoKeyFrame>> videofeatures, String videokey){
		
		int i,j;
		VideoKeyFrame q,r;

		double divergence = 0.0;
		double mindivergence = 100;
		int minvidkeyframe = -1;
		
		List<VideoKeyFrame> querykeyframes = FindKeyFrames();
		List<VideoKeyFrame> videokeyframes = videofeatures.get(videokey);
		Score videoscore = new Score();
		
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
			    	videoscore.framenumbers.add(minvidkeyframe);
			    	videoscore.score.add(mindivergence);
			    }
			  
		return videoscore;
	}

public Object[] MatchVideo(HashMap<String,List<VideoKeyFrame>> videofeatures, HashMap<String,List<AudioKeyFrame>> audiofeatures){
	Object[] videoresults = SequenceMatch(videofeatures);
	Object[] audioresults = audiotrack.SequenceMatch(audiofeatures);
	
	HashMap<String, Double> videomatch = (HashMap<String, Double>) videoresults[0];
	HashMap<String, Double> audiomatch = (HashMap<String, Double>) audioresults[0];
	TreeMap<Double,String> sortedvideos = new TreeMap<Double,String>();		
	
	for(String videokey: videomatch.keySet()){
		double videoscore = videomatch.get(videokey);
		double audioscore = audiomatch.get(videokey);
		System.out.println(videokey +  " video: "+ videoscore + " audio: " + audioscore);
		double totalscore = videoscore;
		double percentage = ((totalscore));
		DecimalFormat formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
		formatter.setRoundingMode( RoundingMode.DOWN );
		String scorepercentage = formatter.format(percentage);
		sortedvideos.put(totalscore, videokey + " (" + percentage + "%)");
	}
	
	Object[] ret = new Object[3];
	ret[0] = sortedvideos.values().toArray();
	ret[1] = videoresults[1];
	ret[2] = audioresults[1];
	
	return ret;
}


}
