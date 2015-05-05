package data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import utils.Constants;
import utils.Helper;

public class ImageContainer extends VideoKeyFrame{
		
	byte[] imagebytes;
	public BufferedImage image;
	
	ImageContainer(String imagefilename){
		
		image = new BufferedImage(Constants.IMAGE_WIDTH, Constants.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		try{	
			File imageFile = new File(imagefilename);
			InputStream imageFileInputStream = new FileInputStream(imageFile);
			
			long len = imageFile.length();
			imagebytes = new byte[(int)len];
			
			int offset = 0, numRead = 0;
			while(offset < imagebytes.length && (numRead = imageFileInputStream.read(imagebytes, offset, imagebytes.length-offset))>=0){
				offset+=numRead;
			}		
			
			imageFileInputStream.close();
			
			} catch (FileNotFoundException e){
				 e.printStackTrace();
			} catch (IOException e){
				 e.printStackTrace();
			}
		
	}
	
	public void DrawImage(){
		
		 int ind = 0;
		 for(int y = 0; y < Constants.IMAGE_HEIGHT; y++){
			 for(int x = 0; x < Constants.IMAGE_WIDTH; x++){
				 
				 byte r = imagebytes[ind];				
				 byte g = imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH];	
				 byte b = imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH*2];
				 
				 int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				 image.setRGB(x, y, pix);
				 ind++; 
			 }
		 }	
	}
	
	public int[][] CreateImageMatrix(){
		int x,y,Y,ind = 0;
		int[][] Ymatrix = new int[Constants.IMAGE_HEIGHT][Constants.IMAGE_WIDTH];
		
		 for(y = 0; y < Constants.IMAGE_HEIGHT; y++){
			 for(x = 0; x < Constants.IMAGE_WIDTH; x++){
				 int R = Helper.ByteToInt(imagebytes[ind]);					
				 int G = Helper.ByteToInt(imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH]);	
				 int B = Helper.ByteToInt(imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH*2]); 
		 
			     Y = Helper.double2int((R * 0.29891 + G * 0.58661 + B * 0.11448));
			     Ymatrix[y][x] = Y;
			     ind++;
			 }
		 }
		 
		return Ymatrix;	
	}
	
	public void GenerateHistogramDistributions(){
		
		 int x,y,ind = 0;
//		 int Y,U,V;
		 int histY[] = new int[256];
		 int histU[] = new int[256];
		 int histV[] = new int[256];
		 
		 for(y = 0; y < Constants.IMAGE_HEIGHT; y++){
			 for(x = 0; x < Constants.IMAGE_WIDTH; x++){
				 
				 int R = Helper.ByteToInt(imagebytes[ind]);					
				 int G = Helper.ByteToInt(imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH]);	
				 int B = Helper.ByteToInt(imagebytes[ind+Constants.IMAGE_HEIGHT*Constants.IMAGE_WIDTH*2]); 
		 
//			     Y = Helper.double2int((R * 0.29891 + G * 0.58661 + B * 0.11448));
//			     U = Math.min(255, (Helper.double2int((R * (-0.16874) - G * 0.33126 + B * 0.5)) + 128));
//			     V = Math.min(255, (Helper.double2int((R * 0.5 - G * 0.41869 - B * 0.08131)) + 128));
				 
//				 Y = Helper.double2int(( 0.299)*R 	+ ( 0.587)*G 	+ ( 0.114)*B);
//				 U = Helper.double2int((-0.147)*R 	+ (-0.289)*G 	+ ( 0.436)*B);
//				 V = Helper.double2int(( 0.615)*R 	+ (-0.515)*G 	+ (-0.100)*B);
			     
				 histY[R]++;
			     histU[G]++;
			     histV[B]++;
				 
				 ind++;
			 }
			
		 }
		 
		 int i = 0;
		 pdfY = new double[256];
		 for (i=0; i < 256; i++){
			 pdfY[i] = (double)histY[i]/(Constants.IMAGE_WIDTH*Constants.IMAGE_HEIGHT);
		 }
		 pdfU = new double[256];
		 for (i=0; i < 256; i++){
			 pdfU[i] = (double)histU[i]/(Constants.IMAGE_WIDTH*Constants.IMAGE_HEIGHT);
		 }
		 pdfV = new double[256];
		 for (i=0; i < 256; i++){
			 pdfV[i] = (double)histV[i]/(Constants.IMAGE_WIDTH*Constants.IMAGE_HEIGHT);
		 }
 
	}
	
}
