package data;

import java.io.Serializable;

public class AudioKeyFrame implements Serializable{
	int index;
	double[] spectrum;
	
	public void setSpectrum(double[] spectrum){
		this.spectrum = spectrum;
	}
	
	public int getIndex(){
		return index;
	}
}
