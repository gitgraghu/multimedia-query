package data;

import java.io.Serializable;

import utils.Constants;
import utils.Helper;

public class VideoKeyFrame implements Serializable{
	int index;
	int motion;
	double[] pdfY;
	double[] pdfU;
	double[] pdfV;
	double[] pdfTex;
	
	
	public void setPdfY(double[] pdfY){
		this.pdfY = pdfY;
	}
	
	public void setPdfU(double[] pdfU){
		this.pdfU = pdfU;
	}
	
	public void setPdfV(double[] pdfV){
		this.pdfV = pdfV;
	}
	
	public void setMotion(int motion){
		this.motion = motion;
	}
	public void setPdfs(double[] pdfY, double[] pdfU, double[] pdfV, double[] pdfTex){
		this.pdfY = pdfY;
		this.pdfU = pdfU;
		this.pdfV = pdfV;
		this.pdfTex = pdfTex;
	}
	
	public void setIndex(int num){
		index = num;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getMotion(){
		return motion;
	}
	public void GenerateTextureHistogram(int[][] Ymatrix){
		String bits="";
		int pixNewValue;
		int hist[] = new int[256];
		
		 for(int i = 1; i < Constants.IMAGE_HEIGHT-1; i++){
			 for(int j = 1; j < Constants.IMAGE_WIDTH-1; j++){
				 bits = "";
				 
				 if(Ymatrix[i-1][j-1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i-1][j]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i-1][j+1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i][j+1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i+1][j+1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i+1][j]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i+1][j-1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 if(Ymatrix[i][j-1]<=Ymatrix[i][j])
					 bits+='0';
				 else
					 bits+='1';
				 
				 pixNewValue = Integer.parseInt(bits,2); 
//				 System.out.println(bits + " val:" + pixNewValue);
				 hist[pixNewValue]++;
			 }
		 }
		 
		 int i = 0;
		 pdfTex = new double[256];
		 for (i = 0; i < 256; i++){
			 pdfTex[i] = (double)hist[i]/((Constants.IMAGE_WIDTH-1)*(Constants.IMAGE_HEIGHT-1));
		 }

	}
}
