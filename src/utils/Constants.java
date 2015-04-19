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
	public final static int VIDEO_FRAMECOUNT = 600;
	public final static int QUERY_FRAMECOUNT = 150;
	public final static double NUM_FFT_WINDOWS = 10;
	public final static int FFT_WINDOW_SIZE = 1024;
	public final static double CUMULATIVE_TRESHOLD = 0.1;
	public final static double DIVERGENCE_TRESHOLD = 0.03;
	public final static int MAX_SAMPLE_VALUE = 32767;
	public final static double FRAME_DELAY = 0.033;
}
