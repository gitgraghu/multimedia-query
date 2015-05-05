package utils;

public class Helper {

	public static int ByteToInt(byte b){
		return (int)b & 0x000000FF;
	}

	 public static int double2int(double color) {
	        
		    int a = (int) Math.round(color);
	        
	        if (a > 255)
	            a = 255;
	        
	        if (a < 0)
	            a = 0;

	        return a;
	    }
	 
	    public static double CalculateDirectedDivergence(double[] p, double[] q){
			
			int i;
			double px,qx,result = 0.0;
			
			for(i = 0; i < p.length; i++){
				px = p[i];
				qx = q[i];
				
				if(qx!=0 && px!=0){
					result = result + (double) ( (qx*(Math.log(qx) - Math.log(px))) + (px*(Math.log(px) - Math.log(qx))) );
				}
			}
			
			return result;
		}
	    
	    public static double CalculateEucledianDistance(double[] p, double[] q){
	    	
	    	int i;
			double px,qx,result = 0.0;
			
			for(i = 0; i < p.length; i++){
				px = p[i];
				qx = q[i];
				result = result + ((px-qx)*(px-qx));
			}
			
			result = Math.sqrt(result);
			return result;
	    	
	    }
}