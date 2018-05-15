package webserver.mss;

import Jama.Matrix;
import Jama.QRDecomposition;

public class LinearRegression {

	private int N;
	private int p;

	private Matrix features;
	private Matrix target;
	
	private Matrix beta; //beta coefficients
	
	private double SST; //Sum Square Total
	private double SSE; //Sum Square Error

	public LinearRegression(){}

	public LinearRegression(double[][] features, double[] target){
		if(features.length != target.length)
			throw new RuntimeException("Dimensions don't match!");

		N = features.length;
		p = features[0].length;
		this.features = new Matrix(features);
		this.target = new Matrix(target, N);	
	}

		
	public void fit(){
		QRDecomposition qr = new QRDecomposition(features);
		beta = qr.solve(target);

		double sum = 0.0;
		for (int i = 0; i < N; i++){
			sum += target.get(i,0);
		}

		double mean = sum / N;

		for(int i = 0; i < N; i++){
			double dev = target.get(i,0) - mean;
			SST += dev * dev;
		}

		Matrix residuals = features.times(beta).minus(target);
		SSE = residuals.norm2() * residuals.norm2();
	}	

	public double predict(double[] x){
		int result = 0;
		
		for(int i = 0; i < p; i++)
			result += x[i] * beta(i);		
	
		return result;
	}

	
	public double beta(int j) {
		return beta.get(j, 0);
	}

	public double R2() {
		return 1.0 - SSE/SST;
	}

	public void setFeatures(double[][] features) {
		p = features.length;
		this.features = new Matrix(features);
	}

	public void setTarget(double[] target){
		N = target.length;
		this.target = new Matrix(target, N);
	}
	
	public Matrix getFeatures(){
		return features;
	}

	/*String with features and target matrices*/
	public String toString(){
		String featuresStr,targetStr;
		featuresStr = targetStr = "[";

		String bigString = "";

		for(int i = 0; i < N; i++){
			featuresStr += "[";
			for(int j = 0; j < p; j++){
				featuresStr += " " + features.get(i,j) + " "; 
			}
			if(i != (N - 1))
				featuresStr += "]\n";
			else
				featuresStr += "]";
		}
		featuresStr += "]";

		for(int i = 0; i < N; i++){
			targetStr += " " + target.get(i,0) + " ";
		}
		targetStr += "]";

		bigString = "FEATURES\n" + featuresStr + "\n" + "TARGET\n" + targetStr;
		
		return bigString;		
	}
}
