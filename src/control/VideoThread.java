package control;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSlider;

import ui.ScoreGraph;
import utils.Constants;
import data.VideoContainer;

public class VideoThread extends Thread {
	
	VideoContainer video;
	ImageIcon frameimage;
	JLabel videobox;
	Clip audioClip;
	JSlider videoslider;
	ScoreGraph videoscoregraph;
	ScoreGraph audioscoregraph;
	ScoreGraph motionscoregraph;
	
	long framedelay;
	int curframe = 0;
	boolean videopaused = false;
	boolean videostarted = false;
	
	public VideoThread(VideoContainer videocontainer, ImageIcon videoframe, JLabel videobox, JSlider videoslider, ScoreGraph videoscoregraph, ScoreGraph audioscoregraph, ScoreGraph motionscoregraph){
		
		this.video 		= videocontainer;
		this.frameimage = videoframe;
		this.videobox 	= videobox; 
		this.videoslider= videoslider;
		this.videoscoregraph = videoscoregraph;
		this.audioscoregraph = audioscoregraph;
		this.motionscoregraph = motionscoregraph;
		
		try {
			audioClip = AudioSystem.getClip();
			audioClip.open(video.audiotrack.getAudioStream());
			framedelay = audioClip.getMicrosecondLength()/(video.getFrameCount() * 1000);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {

		videostarted = true;
		
		while(true){
			for(; curframe < video.getFrameCount(); curframe++){
	
					displayCurrentFrame();
				
					try {
						Thread.sleep(framedelay);
					 } catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		curframe = 0;
		this.stopvideo();	
		}	
	}
	
	public void displayCurrentFrame(){
		frameimage.setImage(video.videoframes[curframe].image);
		videoslider.setValue(curframe);
		videobox.updateUI();
		
		if(videoscoregraph!=null){
			int graphlineX = (int) Math.round(((double)curframe/Constants.VIDEO_FRAMECOUNT)*videoscoregraph.getWidth());
			videoscoregraph.setCurrentX(graphlineX);
			audioscoregraph.setCurrentX(graphlineX);
			motionscoregraph.setCurrentX(graphlineX);
			motionscoregraph.repaint();
			audioscoregraph.repaint();
			videoscoregraph.repaint();
		}
		
	}
	
	public void play(){
		if(videopaused){
			audioClip.start();
			this.resume();
			videopaused = false;
		}
		else 
		if(!videostarted){
			audioClip.start();
			this.start();
		}
	}
	
	public void pause(){
		if(videostarted & !videopaused){
			audioClip.stop();
			this.suspend();
			videopaused = true;
		}
	}
	
	public void stopvideo(){
		if(videostarted){
			audioClip.stop();
			this.suspend();
			curframe = 0;
			audioClip.setFramePosition(0);
			videopaused = true;
		}
	}
	
	public void setCurrentFrame(int framenumber){
		curframe = framenumber;
		audioClip.setMicrosecondPosition(framenumber*framedelay*1000);
	}
	
	public void setVideoContainer(VideoContainer videocontainer){
		video = videocontainer;
		try {
			audioClip.close();
			audioClip.open(video.audiotrack.getAudioStream());
		} catch (LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
		framedelay = audioClip.getMicrosecondLength()/(video.getFrameCount()* 1000);
	}

}
