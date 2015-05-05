package data;

import java.lang.reflect.Array;
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
	public double[] motionvector;
	
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
		if(!isoffline){
			motionvector = GenerateMotionCounts();
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
		int[][] Ymatrix = p.CreateImageMatrix();
		p.GenerateTextureHistogram(Ymatrix);
		k.setPdfs(p.pdfY, p.pdfU, p.pdfV,p.pdfTex);
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
				int[][] YMatrix = q.CreateImageMatrix();
				q.GenerateTextureHistogram(YMatrix);
				k.setPdfs(q.pdfY, q.pdfU, q.pdfV, q.pdfTex);
				keyframes.add(k);
				p = videoframes[i];
			}
		}
		
		return keyframes;
	}
	
	public Object[] SequenceMatch(HashMap<String,List<VideoKeyFrame>> videofeatures, boolean matchcolor, boolean matchtexture){
		
		int i,j;
		VideoKeyFrame q,r;
		HashMap<String,Score> videoscores = new HashMap<String,Score>();
		HashMap<String, Double> videomatch = new HashMap<String, Double>();
		double divergence = 0.0;
		double mindivergence = 100;
		double maxdivergence = 0.0;
		double totaldivergence = 0.0;

		int minvidkeyframe = -1;
		List<VideoKeyFrame> querykeyframes = FindKeyFrames();
		for(String videoname : videofeatures.keySet()){

			List<VideoKeyFrame> videokeyframes = videofeatures.get(videoname);
			Score videoscore = new Score();
			maxdivergence = -1;
			totaldivergence = 0;
			for(i=0; i<querykeyframes.size(); i++){
				
				q = querykeyframes.get(i);
				
				mindivergence = 100;
				minvidkeyframe = -1;

				for(j=0; j<videokeyframes.size(); j++){
					divergence = 0.0;
					r = videokeyframes.get(j);
					if(matchtexture){
						divergence = divergence + Helper.CalculateDirectedDivergence(q.pdfTex, r.pdfTex);
					}
					if(matchcolor || (!matchcolor && !matchtexture)){
						divergence = divergence + CalculateDirectedDivergence(q, r);
					}
					
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
			        totaldivergence+=mindivergence;

			    	if(mindivergence > maxdivergence){
			    		maxdivergence=mindivergence;	
			    	}
			}
			
			videoscores.put(videoname, videoscore);
			videomatch.put(videoname, totaldivergence/querykeyframes.size());
				
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

public Object[] MatchVideo(HashMap<String,List<VideoKeyFrame>> videofeatures, HashMap<String,List<AudioKeyFrame>> audiofeatures, HashMap<String,double[]> motionfeatures, boolean matchcolor, boolean matchtexture, boolean matchaudio, boolean matchmotion){
	Object[] videoresults = SequenceMatch(videofeatures, matchcolor, matchtexture);
	Object[] audioresults = audiotrack.SequenceMatch(audiofeatures);
	Object[] motionresults = calculateMotionScore(motionfeatures);
	HashMap<String, Double> videomatch = (HashMap<String, Double>) videoresults[0];
	HashMap<String, Double> audiomatch = (HashMap<String, Double>) audioresults[0];
	HashMap<String, Double> motionmatch = (HashMap<String, Double>) motionresults[0];
	TreeMap<Double,String> sortedvideos = new TreeMap<Double,String>(Collections.reverseOrder());		
	
	double maxvidmatchval = this.FindMaxValue(videomatch);
	double maxaudmatchval = this.FindMaxValue(audiomatch);
	double maxmotmatchval = this.FindMaxValue(motionmatch)/100;
	System.out.println("SCORES:");
	for(String videokey: videomatch.keySet()){
				
		double videoscore = videomatch.get(videokey);
		double audioscore = audiomatch.get(videokey);
		double motionscore = motionmatch.get(videokey);
		System.out.println(videokey + "    \tvideo: " + videoscore + "    \taudio: " + audioscore + "    \tmotion: " + motionscore);
		videoscore = 100.0 - ((videoscore/maxvidmatchval)*100.0);
		audioscore = 100.0 - ((audioscore/maxaudmatchval)*100.0);
		motionscore = 100.0 - ((motionscore/maxmotmatchval)*100.0);
		if(motionscore<0){
			motionscore = 0;
		}

		DecimalFormat formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
		formatter.setRoundingMode( RoundingMode.DOWN );
		String audioscorepercentage = formatter.format(audioscore);
		String videoscorepercentage = formatter.format(videoscore);
		String motionscorepercentage = formatter.format(motionscore);
		
//		double totalscore = 0.0;
//		if(matchaudio){
//			if(matchcolor || matchtexture){
//				totalscore = Constants.VIDEO_WEIGHT*videoscore + Constants.AUDIO_WEIGHT*audioscore;
//				String totalscorepercentage = formatter.format(totalscore);
//				sortedvideos.put(totalscore, videokey + " (V:" + videoscorepercentage + "%, A:" + audioscorepercentage + "%, M: " + motionscorepercentage + "T:" + totalscorepercentage + "%)");
//			}
//			else{
//				sortedvideos.put(audioscore, videokey + " (A:" + audioscorepercentage + "%)");
//			}
//		}
//		else{
//				sortedvideos.put(videoscore, videokey + " (V:" + videoscorepercentage + "%)");
//		}
		
		String videowithscores = videokey + " (";
		double totalscore = -1.0;
		double weights = 1.0;
		boolean matchvideo = matchcolor || matchtexture;
		if(matchvideo){
			totalscore = videoscore;
			videowithscores = videowithscores + "V:" + videoscorepercentage +"% ";
		}
		if(matchaudio){
			 if(totalscore == -1.0){
			 totalscore = audioscore;
			 }
			 else{
			 totalscore = Constants.VIDEO_WEIGHT*totalscore + Constants.AUDIO_WEIGHT*audioscore;
			 weights = Constants.AUDIO_WEIGHT + Constants.VIDEO_WEIGHT;
			 }
			 videowithscores = videowithscores + "A:" + audioscorepercentage +"% ";
		}
		if(matchmotion){
			 if(totalscore == -1.0){
				 totalscore = motionscore;
			 }
			 else{
				 if(matchaudio && !matchvideo){
					 totalscore = totalscore*Constants.AUDIO_WEIGHT + Constants.MOTION_WEIGHT*motionscore;
					 weights = Constants.AUDIO_WEIGHT + Constants.MOTION_WEIGHT;
				 }
				 else if(matchvideo && !matchaudio){
					 totalscore = totalscore*Constants.VIDEO_WEIGHT + Constants.MOTION_WEIGHT*motionscore;
					 weights = Constants.VIDEO_WEIGHT + Constants.MOTION_WEIGHT;
				 }
				 else{
				 totalscore = totalscore + Constants.MOTION_WEIGHT*motionscore;
				 weights = weights + Constants.MOTION_WEIGHT;
				 }
			 }
			 videowithscores = videowithscores + "M:" + motionscorepercentage +"% ";
		}
		
		totalscore = totalscore/weights;
		String totalscorepercentage = formatter.format(totalscore);
		videowithscores = videowithscores + "T: " + totalscorepercentage + "%)";
		sortedvideos.put(totalscore,videowithscores);
	}
	
	Object[] ret = new Object[4];
	ret[0] = sortedvideos.values().toArray();
	ret[1] = videoresults[1];
	ret[2] = audioresults[1];
	ret[3] = motionresults[1];
	return ret;
}

private double FindMaxValue(HashMap<String,Double> map){
	
		double maxval = -1.0;
	for(String videokey: map.keySet()){
		
		double val = map.get(videokey);
		if(val > maxval){
			maxval = val;
		}
	}
	
	return maxval;
}

private int FindMotion(int framenum){

	int [][][] motiondiffs = new int[Constants.NUM_MOTION_FRAMES][Constants.IMAGE_HEIGHT][Constants.IMAGE_WIDTH];

	for(int k=0; k<Constants.NUM_MOTION_FRAMES-1; k++){
		
				ImageContainer framenext = videoframes[framenum+k+1];
				ImageContainer frameprev = videoframes[framenum+k];

				int [][]nextimg = framenext.CreateImageMatrix();
				int [][]previmg = frameprev.CreateImageMatrix();
				
				for(int i=0; i<Constants.IMAGE_HEIGHT ; i++){
					for(int j=0; j<Constants.IMAGE_WIDTH; j++){
				
						int diff = Math.abs(nextimg[i][j] - previmg[i][j]);
						
						if(diff > Constants.MOTION_PIXEL_TRESHOLD){
							motiondiffs[k][i][j] = 1;
						}
						else{
							motiondiffs[k][i][j] = 0;
						}
					}
				}
	}
	
	int i,j,k,l;
	int count=0;
	for(i=0; i<Constants.IMAGE_HEIGHT ; i++){
		for(j=0; j<Constants.IMAGE_WIDTH; j++){
			for(l=0; l<Constants.NUM_MOTION_FRAMES-Constants.PATTERN; l++){
				for(k=0; k<Constants.PATTERN; k++){
					if(motiondiffs[l+k][i][j]!=1){
						l = l+k;
						break;
					}
				}			
			if (k==(Constants.PATTERN-1)){
				count++;
			}
		  }
		}
	}
	
	return count;
	
}

public double[] GenerateMotionCounts(){
	
	double count = 0.0;
	double ret[] = new double[getFrameCount()/Constants.NUM_MOTION_FRAMES];
	int k=0;
	for(int i =0; i< getFrameCount(); i+=Constants.NUM_MOTION_FRAMES){
		count = FindMotion(i);
		ret[k] = count;
		k++;
	}

	return ret;
}

public double[] videomotionsum(double[] motionvector){
	
	int queryvectorlength = Constants.QUERY_FRAMECOUNT/Constants.NUM_MOTION_FRAMES;
	
	double sum = 0;
	double res[] = new double[motionvector.length - queryvectorlength + 1];
	
	
	for(int i=0; i<(motionvector.length - queryvectorlength + 1); i++){
		
		sum = 0.0;
		for(int j=0; j<queryvectorlength; j++){
			sum = sum + motionvector[i+j];
		}
		
		res[i] = sum;
	}
	return res;
}

public double querymotionsum(double[] querymotionvector){
	double sum = 0.0;
	for(int i=0; i<querymotionvector.length ; i++){
		sum = sum + querymotionvector[i];
	}
	return sum;
}

public Object[] calculateMotionScore(HashMap<String, double[]> motionfeatures){
	
	double[] videomotionvector;
	double querymotionsum = querymotionsum(motionvector);
	int videovectorlength = Constants.VIDEO_FRAMECOUNT/Constants.NUM_MOTION_FRAMES;
	int queryvectorlength = Constants.QUERY_FRAMECOUNT/Constants.NUM_MOTION_FRAMES;
	
	HashMap<String,Score> motionscores = new HashMap<String,Score>();
	HashMap<String,Double> motionmatch = new HashMap<String, Double>();
	double min;
	double diff = 0.0;
	int minframenum = 0;
	
	for(String videokey: motionfeatures.keySet()){

		videomotionvector = motionfeatures.get(videokey);
		double[] videomotionsums = videomotionsum(videomotionvector);
		
		min = 10000000.0;

		for(int i=0; i<(videovectorlength - queryvectorlength - 1); i++){
			diff = Math.abs(videomotionsums[i] - querymotionsum);
			if(diff < min){
				min = diff;
				minframenum = i*Constants.NUM_MOTION_FRAMES;
			}
		}
		
		Score motionscore = new Score();

		motionscore.framenumbers.add(minframenum);
		motionscore.score.add(min);
		motionscore.framenumbers.add(minframenum + Constants.QUERY_FRAMECOUNT);
		motionscore.score.add(min);

		motionscores.put(videokey, motionscore);
		motionmatch.put(videokey, min);

	}
	
	Object ret[] = new Object[2];
	ret[0] = motionmatch;
	ret[1] = motionscores;
	return ret;
}

//public void calculateMotionScore(HashMap<String, double[]> motion)
//{
//	int videovectorlength = Constants.VIDEO_FRAMECOUNT/Constants.NUM_MOTION_FRAMES;
//	int queryvectorlength = Constants.QUERY_FRAMECOUNT/Constants.NUM_MOTION_FRAMES;
//	double[] score = new double[videovectorlength];
//	double[] videovector = new double[queryvectorlength];
//	double[] videomotionvector;
//	
//	
//	
//	for(String videokey: motion.keySet()){
//		System.out.println(videokey);
//		videomotionvector = motion.get(videokey);
//		int k,j=0;
//		for (int i=0;i<(videomotionvector.length-queryvectorlength);i++)
//		{
//			k=i;j=0;
//			while(j < queryvectorlength){
//				videovector[j]=videomotionvector[k];
//				j++;
//				k++;
//		}
//		score[i]=Helper.CalculateEucledianDistance(motionvector, videovector);
//		System.out.println(score[i]);
//		}
//	}
//}


}
