package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JList;

import java.awt.Font;

import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JButton;

import utils.Constants;
import control.VideoThread;
import data.AudioKeyFrame;
import data.VideoKeyFrame;
import data.VideoContainer;
import data.Score;

import java.awt.Canvas;

import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;

public class MultimediaProject {
	
	private JFrame frmMultimediaQuery;
	private BufferedImage blankframe;
	
	private JList<String> querylist;
	private JScrollPane scrollPane;
	private JSlider queryslider;
	private JLabel querybox;
	private ImageIcon queryframe;
	private JButton queryPlay;
	private JButton queryPause;
	private JButton queryStop;
	private ScoreGraph videoscoregraph;
	private ScoreGraph audioscoregraph;
	private ScoreGraph motionscoregraph;

	private JList<String> videolist;
	private JSlider videoslider;
	private JLabel videobox;
	private ImageIcon videoframe;
	private JButton videoPlay;
	private JButton videoPause;
	private JButton videoStop;
	
	private JCheckBox chckbxColor;
	private JCheckBox chckbxTexture;
	private JCheckBox chckbxAudio;
	private JCheckBox chckbxMotion;
	Object[] videonames = new String[] {"flowers", "interview", "movie", "musicvideo", "sports", "StarCraft", "traffic"};
	String[] queryvidnames = new String[] {"first","second","Q3_","Q4_","Q5_","Q6_","Q7_","HQ1_","HQ2_","HQ3_","HQ4_"};
	
	HashMap <String,Score> videoscores;
	HashMap <String,Score> audioscores;
	HashMap <String,Score> motionscores;
	VideoContainer currentVideo;
	VideoContainer queryVideo;
	
	String videokey;
	String querykey;
	
	VideoThread videoThread;
	VideoThread queryThread;
	
	HashMap<String,List<VideoKeyFrame>> videofeatures;
	HashMap<String, double[]> motionfeatures;
	HashMap<String, List<AudioKeyFrame>> audiofeatures;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MultimediaProject window = new MultimediaProject();
					window.frmMultimediaQuery.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MultimediaProject() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void ResetVideolist(){
		int i = 0;
		for(i = 0; i< videonames.length; i++){
			videonames[i] = ((String)videonames[i]).split(" ")[0];
		}
	}
	
	private void initialize() {
		
		//MAIN FRAME
		frmMultimediaQuery = new JFrame();
		frmMultimediaQuery.setFont(new Font("Droid Sans", Font.PLAIN, 12));
		frmMultimediaQuery.setResizable(false);
		frmMultimediaQuery.setTitle("Multimedia Query");
		frmMultimediaQuery.setBounds(100, 50, 767, 655);
		frmMultimediaQuery.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmMultimediaQuery.getContentPane().setLayout(null);
		
		//BLANK VIDEOFRAME
		blankframe = new BufferedImage(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		//MATCH VIDEO BUTTON
		JButton btnMatchVideo = new JButton("Match Video");
		btnMatchVideo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object[] ret = queryVideo.MatchVideo(videofeatures, audiofeatures, motionfeatures, chckbxColor.isSelected(), chckbxTexture.isSelected(), chckbxAudio.isSelected(), chckbxMotion.isSelected());
				videonames = (Object[]) ret[0];
				videoscores = (HashMap<String, Score>) ret[1];
				audioscores = (HashMap<String, Score>) ret[2];
				motionscores= (HashMap<String, Score>) ret[3];
				videolist.clearSelection();
				videolist.updateUI();
			}
		});
		btnMatchVideo.setBounds(246, 139, 131, 25);
		frmMultimediaQuery.getContentPane().add(btnMatchVideo);
		
		//QUERY LIST
		querylist = new JList<String>();
		querylist.setVisibleRowCount(7);
		querylist.setFont(new Font("Droid Sans", Font.BOLD, 12));
		querylist.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Query List", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
		querylist.setBounds(105, 12, 131, 144);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(105,12,131,144);
		scrollPane.setViewportView(querylist);
		frmMultimediaQuery.getContentPane().add(scrollPane);
		querylist.setModel(new AbstractListModel() {
			String[] values = queryvidnames;
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		querylist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		querylist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
				String videokey = (String) querylist.getSelectedValue();
				queryVideo = new VideoContainer(Constants.QUERY_PATH + videokey+"/"+videokey, Constants.QUERY_FRAMECOUNT, false, true);	
				if(queryThread == null){
					queryThread = new VideoThread(queryVideo, queryframe, querybox, queryslider, null, null, null);
					queryPlay.setEnabled(true);
				}
				else{
					queryThread.stopvideo();
					queryPlay.setEnabled(true);
					queryPause.setEnabled(false);
					queryStop.setEnabled(false);
					queryThread.setVideoContainer(queryVideo);
					queryThread.setCurrentFrame(0);
					queryslider.setValue(0);
					queryThread.displayCurrentFrame();
					ResetVideolist();
					videolist.updateUI();
				}
				}
			}			
		});
		
		//QUERY SLIDER
		queryslider = new JSlider();
		queryslider.setPaintLabels(true);
		queryslider.setMaximum(Constants.QUERY_FRAMECOUNT);
		queryslider.setValue(0);
		queryslider.setBounds(12, 193, 352, 16);
		queryslider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				queryThread.pause();
				queryPause.setEnabled(false);
				queryPlay.setEnabled(true);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				queryThread.setCurrentFrame(queryslider.getValue());
				queryThread.displayCurrentFrame();
			}
		});
		frmMultimediaQuery.getContentPane().add(queryslider);
		
		//QUERY VIDEO BOX
		queryframe = new ImageIcon(blankframe);
		querybox = new JLabel(queryframe);
		querybox.setBounds(12, 221, 352, 288);
		frmMultimediaQuery.getContentPane().add(querybox);

		//QUERY PLAYBUTTON
		queryPlay = new JButton("PLAY");
		queryPlay.setBounds(12, 590, 100, 25);
		queryPlay.setEnabled(false);
		queryPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				queryThread.play();
				queryPlay.setEnabled(false);
				queryPause.setEnabled(true);
				queryStop.setEnabled(true);
				}
		});
		frmMultimediaQuery.getContentPane().add(queryPlay);
		
		
		//QUERY PAUSEBUTTON
		queryPause = new JButton("PAUSE");
		queryPause.setBounds(136, 590, 100, 25);
		queryPause.setEnabled(false);
		queryPause.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				queryThread.pause();
				queryPause.setEnabled(false);
				queryPlay.setEnabled(true);

			}
		});
		frmMultimediaQuery.getContentPane().add(queryPause);
		
		//QUERY STOPBUTTON
		queryStop = new JButton("STOP");
		queryStop.setBounds(264, 590, 100, 25);
		queryStop.setEnabled(false);
		queryStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					queryThread.stopvideo();
					queryThread.displayCurrentFrame();
					queryStop.setEnabled(false);
					queryPause.setEnabled(false);
					queryPlay.setEnabled(true);				
			}
		});
		frmMultimediaQuery.getContentPane().add(queryStop);
		
		//VIDEO LIST
		videolist = new JList<String>();
		videolist.setModel(new AbstractListModel() {
			public int getSize() {
				return videonames.length;
			}
			public Object getElementAt(int index) {
				return videonames[index];
			}
		});
		
		videolist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		videolist.setFont(new Font("Droid Sans", Font.BOLD, 12));
		videolist.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Video List", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 255)));
		videolist.setBounds(389, 12, 349, 144);
		videolist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
				String videokey = (String) videolist.getSelectedValue();
				if(videokey!=null){
				videokey = videokey.split(" ")[0];
				currentVideo = new VideoContainer(Constants.VIDEO_PATH + videokey+"/"+videokey, Constants.VIDEO_FRAMECOUNT, false, false);	
				if(videoThread == null){
					videoThread = new VideoThread(currentVideo, videoframe, videobox, videoslider, videoscoregraph, audioscoregraph, motionscoregraph);
					videoPlay.setEnabled(true);
				}
				else{
					videoThread.stopvideo();
					videoPlay.setEnabled(true);
					videoPause.setEnabled(false);
					videoStop.setEnabled(false);
					videoThread.setVideoContainer(currentVideo);
					videoThread.setCurrentFrame(0);
					videoslider.setValue(0);
					videoThread.displayCurrentFrame();
				}
				
				if(videoscores!=null){
					Score score = queryVideo.ReverseMatch(videofeatures, videokey);
					videoscoregraph.createGraphPoints(score,Constants.VIDEO_GRAPH_SCALE);
					videoscoregraph.repaint();
				}
				if(audioscores!=null){
					Score score = queryVideo.audiotrack.ReverseMatch(audiofeatures, videokey);
					audioscoregraph.createGraphPoints(score,Constants.AUDIO_GRAPH_SCALE);
					audioscoregraph.repaint();
				 }		
				if(motionscores!=null){
					motionscoregraph.createGraphPoints(motionscores.get(videokey),Constants.MOTION_GRAPH_SCALE);
					motionscoregraph.repaint();
				 }
			   }
			 }
			}			
		});
		frmMultimediaQuery.getContentPane().add(videolist);
		
		//VIDEO SLIDER
		videoslider = new JSlider();
		videoslider.setMaximum(Constants.VIDEO_FRAMECOUNT);
		videoslider.setValue(0);
		videoslider.setBounds(386, 193, 352, 16);
		videoslider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				videoThread.pause();
				videoPause.setEnabled(false);
				videoPlay.setEnabled(true);
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				videoThread.setCurrentFrame(videoslider.getValue());
				videoThread.displayCurrentFrame();
			}
		});
		frmMultimediaQuery.getContentPane().add(videoslider);
		
		//VIDEO BOX
		videoframe = new ImageIcon(blankframe);
		videobox = new JLabel(videoframe);
		videobox.setBounds(386, 221, 352, 288);
		frmMultimediaQuery.getContentPane().add(videobox);
		
		//VIDEO PLAYBUTTON
		videoPlay = new JButton("PLAY");
		videoPlay.setBounds(391, 590, 100, 25);
		videoPlay.setEnabled(false);
		videoPlay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				videoThread.play();
				videoPlay.setEnabled(false);
				videoPause.setEnabled(true);
				videoStop.setEnabled(true);
				}
		});
		frmMultimediaQuery.getContentPane().add(videoPlay);
		
		//VIDEO PAUSEBUTTON
		videoPause = new JButton("PAUSE");
		videoPause.setBounds(515, 590, 100, 25);
		videoPause.setEnabled(false);
		videoPause.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				videoThread.pause();
				videoPause.setEnabled(false);
				videoPlay.setEnabled(true);

			}
		});
		frmMultimediaQuery.getContentPane().add(videoPause);
		
		//VIDEO STOPBUTTON
		videoStop = new JButton("STOP");
		videoStop.setBounds(638, 590, 100, 25);
		videoStop.setEnabled(false);
		videoStop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
					videoThread.stopvideo();
					videoThread.displayCurrentFrame();
					videoStop.setEnabled(false);
					videoPause.setEnabled(false);
					videoPlay.setEnabled(true);				
			}
		});
		frmMultimediaQuery.getContentPane().add(videoStop);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(386, 521, 356, 57);
		frmMultimediaQuery.getContentPane().add(tabbedPane);
		
		//VIDEO SCORE GRAPH
		videoscoregraph = new ScoreGraph();
		tabbedPane.addTab("Video Graph", null, videoscoregraph, null);
		videoscoregraph.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int selectedframe = (int) Math.round(((double)x/videoscoregraph.getWidth())*Constants.VIDEO_FRAMECOUNT);
				videoThread.setCurrentFrame(selectedframe);
				videoThread.displayCurrentFrame();
			}
		});
		videoscoregraph.setBackground(Color.WHITE);
		
		//AUDIO SCORE GRAPH
		audioscoregraph = new ScoreGraph();
		tabbedPane.addTab("Audio Graph", null, audioscoregraph, null);
		audioscoregraph.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int selectedframe = (int) Math.round(((double)x/audioscoregraph.getWidth())*Constants.VIDEO_FRAMECOUNT);
				videoThread.setCurrentFrame(selectedframe);
				videoThread.displayCurrentFrame();
			}
		});
		audioscoregraph.setBackground(Color.WHITE);
		
		//MOTION SCORE GRAPH
		motionscoregraph = new ScoreGraph();
		tabbedPane.addTab("Motion Graph", null, motionscoregraph, null);
		motionscoregraph.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int selectedframe = (int) Math.round(((double)x/motionscoregraph.getWidth())*Constants.VIDEO_FRAMECOUNT);
			videoThread.setCurrentFrame(selectedframe);
			videoThread.displayCurrentFrame();
			}
		});
		motionscoregraph.setBackground(Color.WHITE);
		
		chckbxColor = new JCheckBox("Color");
		chckbxColor.setSelected(true);
		chckbxColor.setBounds(264, 27, 129, 23);
		frmMultimediaQuery.getContentPane().add(chckbxColor);
		
		chckbxTexture = new JCheckBox("Texture");
		chckbxTexture.setSelected(true);
		chckbxTexture.setBounds(264, 54, 129, 23);
		frmMultimediaQuery.getContentPane().add(chckbxTexture);
		
		chckbxAudio = new JCheckBox("Audio");
		chckbxAudio.setSelected(true);
		chckbxAudio.setBounds(264, 81, 129, 23);
		frmMultimediaQuery.getContentPane().add(chckbxAudio);
		
		chckbxMotion = new JCheckBox("Motion");
		chckbxMotion.setSelected(true);
		chckbxMotion.setBounds(264, 108, 129, 23);
		frmMultimediaQuery.getContentPane().add(chckbxMotion);
		
		
		//INITIALIZE VIDEO FEATURES OBJECT
		File inputfile = new File(Constants.DATA_PATH + Constants.VIDEO_DATA_FILE);
	    ObjectInputStream objectinputstream = null;
		try {
			objectinputstream = new ObjectInputStream(new FileInputStream(inputfile));
			videofeatures = (HashMap<String, List<VideoKeyFrame>>) objectinputstream.readObject();
			objectinputstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		//INITIALIZE AUDIO FEATURES OBJECT
		inputfile  = new File(Constants.DATA_PATH +  Constants.AUDIO_DATA_FILE);
		try {
			objectinputstream = new ObjectInputStream(new FileInputStream(inputfile));
			audiofeatures = (HashMap<String, List<AudioKeyFrame>>) objectinputstream.readObject();
			objectinputstream.close();
		} catch (IOException e) {
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
				e.printStackTrace();
		}
		
		//INITIALIZE MOTION FEATURES OBJECT
		inputfile = new File(Constants.DATA_PATH + Constants.MOTION_DATA_FILE);
	    objectinputstream = null;
		try {
			 objectinputstream = new ObjectInputStream(new FileInputStream(inputfile));
			 motionfeatures = (HashMap<String, double[]>) objectinputstream.readObject();
			 objectinputstream.close();
			} catch (IOException e) {
					e.printStackTrace();
			} catch (ClassNotFoundException e) {
					e.printStackTrace();
			}		
			
	}
}

