package ui;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.Constants;
import data.Score;

public class ScoreGraph extends Canvas{
	
	private int pointWidth = 4;
	private int currentX = 0;
	private Color lineColor = new Color(44, 102, 230, 180);
	private Color pointColor = new Color(100, 100, 100, 180);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	List<Point> graphPoints;
	
	ScoreGraph(){
		graphPoints = new ArrayList<Point>();
	}
	
	public void createGraphPoints(Score vidscore,double yscale){
		graphPoints.clear();
		
		int i,framenum,x,y;
		double score;

		for(i = 0; i < vidscore.framenumbers.size(); i++){
			framenum = vidscore.framenumbers.get(i);
			score = vidscore.score.get(i);
//			System.out.println("framenum" + framenum + "score:" + score);
			x = (int) Math.round((framenum/(double)Constants.VIDEO_FRAMECOUNT)*getWidth());
			y = (int) Math.round((score/yscale)*getHeight());
			graphPoints.add(new Point(x,y));
		}
	}
	
	  @Override
	  	public void paint(Graphics g) {
		  super.paint(g);
		  Graphics2D g2 = (Graphics2D) g;
		  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		  g2.drawLine(0, getHeight(), 0, 0);
		  g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
		  g2.drawLine(currentX, getHeight(), currentX, 0);
		  
		  	  Stroke oldStroke = g2.getStroke();
		  	  g2.setColor(lineColor);
		  	  g2.setStroke(GRAPH_STROKE);
	          for (int i = 0; i < graphPoints.size() - 1; i++) {
	              int x1 = graphPoints.get(i).x;
	              int y1 = graphPoints.get(i).y;
	              int x2 = graphPoints.get(i + 1).x;
	              int y2 = graphPoints.get(i + 1).y;
	              g2.drawLine(x1, y1, x2, y2);
	          }
	          
	          g2.setStroke(oldStroke);
	          g2.setColor(pointColor);
	          for (int i = 0; i < graphPoints.size(); i++) {
	              int x = graphPoints.get(i).x - pointWidth / 2;
	              int y = graphPoints.get(i).y - pointWidth / 2;
	              int ovalW = pointWidth;
	              int ovalH = pointWidth;
	              g2.fillOval(x, y, ovalW, ovalH);
	          }
	  }
	  
	  public void setCurrentX(int x){
		  currentX = x;
	  }
	  

}
