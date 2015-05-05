package utils;

public class Constants {

	public final static int IMAGE_WIDTH  = 352;
	public final static int IMAGE_HEIGHT = 288;
	public final static String MULT_PATH = "/home/graghu/mult";
	public final static String VIDEO_PATH = MULT_PATH + "/videos/";
	public final static String QUERY_PATH = MULT_PATH + "/query/";
	public final static String DATA_PATH =  MULT_PATH + "/data/";
	public final static String VIDEO_DATA_FILE = "videofeatures";
	public final static String AUDIO_DATA_FILE = "audiofeatures";
	public final static String MOTION_DATA_FILE = "motionfeatures";
	public final static int VIDEO_FRAMECOUNT = 600;
	public final static int QUERY_FRAMECOUNT = 150;
	public final static double NUM_FFT_WINDOWS = 10;
	public final static int FFT_WINDOW_SIZE = 4096;
//	public final static double CUMULATIVE_TRESHOLD = 0.1;
//	public final static double DIVERGENCE_TRESHOLD = 0.03;
	public final static double CUMULATIVE_TRESHOLD = 3;
	public final static double DIVERGENCE_TRESHOLD = 1;
	public final static int MAX_SAMPLE_VALUE = 32767;
	public final static double FRAME_DELAY = 0.033;
	public final static double AUDIO_GRAPH_SCALE = 30;
	public final static double VIDEO_GRAPH_SCALE = 5;
	public final static double MOTION_GRAPH_SCALE = 1000;
	public final static double AUDIO_WEIGHT = 0.2;
	public final static double VIDEO_WEIGHT = 0.6;
	public final static double MOTION_WEIGHT = 0.2;
	public final static int NUM_MOTION_FRAMES = 30;
	public final static int MOTION_PIXEL_TRESHOLD = 5;
	public static final int PATTERN = 7;		
}
