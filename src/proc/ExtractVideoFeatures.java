package proc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import utils.Constants;
import data.VideoKeyFrame;
import data.VideoContainer;

public class ExtractVideoFeatures {
		
	public static void main(String args[]){		
		 HashMap<String,List<VideoKeyFrame>> videofeatures = new HashMap<String,List<VideoKeyFrame>>();
		 ProcessVideoFeatures(videofeatures);
		 WriteToDataFile(videofeatures);
	}
	
	private static void ProcessVideoFeatures(HashMap<String,List<VideoKeyFrame>> videofeatures){

		 File videodir = new File(Constants.VIDEO_PATH);
		 File[] videoListing = videodir.listFiles();
		  if (videoListing != null) {
		    for (File child : videoListing) {
		    	String videoname = child.getName();
		    	VideoContainer video = new VideoContainer(child.getAbsolutePath() +"/" +  videoname, Constants.VIDEO_FRAMECOUNT, true,true);
		    	videofeatures.put(videoname, video.FindKeyFrames());
		    	System.out.println(videoname + " Features Extracted..");
		    }
		  }
		  System.out.println("Complete !!");
	}
	
	private static void WriteToDataFile(HashMap<String,List<VideoKeyFrame>> videofeatures){
		File datafile = new File(Constants.DATA_PATH + Constants.VIDEO_DATA_FILE);
       ObjectOutputStream objectoutstream = null;
		try {
			objectoutstream = new ObjectOutputStream(new FileOutputStream(datafile));
			objectoutstream.writeObject(videofeatures);
			objectoutstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
