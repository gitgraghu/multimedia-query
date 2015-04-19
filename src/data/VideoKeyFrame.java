package data;

import java.io.Serializable;

public class VideoKeyFrame implements Serializable{
	int index;
	double[] pdfY;
	double[] pdfU;
	double[] pdfV; 
	
	public void setPdfY(double[] pdfY){
		this.pdfY = pdfY;
	}
	
	public void setPdfU(double[] pdfU){
		this.pdfU = pdfU;
	}
	
	public void setPdfV(double[] pdfV){
		this.pdfV = pdfV;
	}
	
	public void setPdfs(double[] pdfY, double[] pdfU, double[] pdfV){
		this.pdfY = pdfY;
		this.pdfU = pdfU;
		this.pdfV = pdfV;
	}
	
	public void setIndex(int num){
		index = num;
	}
	
	public int getIndex(){
		return index;
	}
}
