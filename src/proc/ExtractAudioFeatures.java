package proc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import utils.Constants;
import data.AudioContainer;
import data.AudioKeyFrame;

public class ExtractAudioFeatures {

	public static void main(String args[]){		
		 HashMap<String,List<AudioKeyFrame>> audiofeatures = new HashMap<String,List<AudioKeyFrame>>();
		 ProcessAudioFeatures(audiofeatures);
		 WriteToDataFile(audiofeatures);
	}
	
	private static void ProcessAudioFeatures(HashMap<String,List<AudioKeyFrame>> audiofeatures){

		 File videodir = new File(Constants.VIDEO_PATH);
		 File[] videoListing = videodir.listFiles();
		  if (videoListing != null) {
		    for (File child : videoListing) {
		    	String videoname = child.getName();
		    	AudioContainer audio = new AudioContainer(child.getAbsolutePath() + "/" + videoname + ".wav", true, false);
		    	audiofeatures.put(videoname, audio.GenerateAudioKeyFrames());
		    	System.out.println(videoname + " Features Extracted..");
		    }
		  }
		  System.out.println("Complete !!");
	}
	
	private static void WriteToDataFile(HashMap<String,List<AudioKeyFrame>> videofeatures){
		File datafile = new File(Constants.DATA_PATH + Constants.AUDIO_DATA_FILE);
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
