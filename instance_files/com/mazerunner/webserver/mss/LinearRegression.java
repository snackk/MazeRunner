package com.mazerunner.webserver.mss;


public class LinearRegression {

	private int[][] features;
	private int[] target;

	public LinearRegression(){}

	public LinearRegression(int[][] features, int[] target){
		this.features = features;
		this.target = target;	
	}

	
	

	/*Getters and Setters*/	
	public void setFeatures(int[][] features) {
		this.features = features;
	}

	public void setTarget(int[] target){
		this.target = target;
	}

	public int[][] getFeatures(){
		return features;
	}
	
	public int[] getTarget(){
		return target;
	}
	
	public String toString(){
		String featuresStr,targetStr;
		featuresStr = targetStr = "[";

		String bigString = "";

		for(int i = 0; i < features.length; i++){
			featuresStr += "[";
			for(int j = 0; j < features[i].length; j++){
				featuresStr += " " + features[i][j] + " "; 
			}
			if(i != (features.length - 1))
				featuresStr += "]\n";
			else
				featuresStr += "]";
		}
		featuresStr += "]";

		for(int i = 0; i < target.length; i++){
			targetStr += " " + target[i] + " ";
		}
		targetStr += "]";

		bigString = "FEATURES\n" + featuresStr + "\n" + "TARGET\n" + targetStr;
		
		return bigString;		
	}
}
