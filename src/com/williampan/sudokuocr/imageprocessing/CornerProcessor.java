package com.williampan.sudokuocr.imageprocessing;

import org.opencv.core.Point;

public class CornerProcessor {
	
	/**
	 * Find topleft corner point
	 * Calculated using max(x+y)
	 * @param points Array of corners
	 */
	public double[] topLeftPoint(Point[] points){
		double[] retVal = {Double.MAX_VALUE, Double.MAX_VALUE};
		for (Point point : points){
			double pointSum = point.x + point.y;
			double retSum = retVal[0] + retVal[1];
			if (pointSum < retSum){
				retVal[0] = point.x;
				retVal[1] = point.y;
			}
		}
		return retVal;
	}
	
	/**
	 * Find botright corner point
	 * Calculated using min(x+y)
	 * @param points Array of corners
	 */
	public double[] botRightPoint(Point[] points){
		double[] retVal = {Double.MIN_VALUE, Double.MIN_VALUE};
		for (Point point : points){
			double pointSum = point.x + point.y;
			double retSum = retVal[0] + retVal[1];
			if (pointSum > retSum){
				retVal[0] = point.x;
				retVal[1] = point.y;
			}
		}
		return retVal;
	}
	
	/**
	 * Find botleft corner point
	 * Calculated using min(y-x)
	 * @param points Array of corners
	 */
	public double[] botLeftPoint(Point[] points){
		double[] retVal = {Double.MIN_VALUE, Double.MIN_VALUE};
		for (Point point : points){
			double pointSum = point.y - point.x;
			double retSum = retVal[1] - retVal[0];
			if (pointSum > retSum){
				retVal[0] = point.x;
				retVal[1] = point.y;
			}
		}
		return retVal;
	}
	
	/**
	 * Find topright corner point
	 * Calculated using max(y-x)
	 * @param points Array of corners
	 */
	public double[] topRightPoint(Point[] points){
		double[] retVal = {Double.MAX_VALUE, Double.MAX_VALUE};
		for (Point point : points){
			double pointSum = point.y - point.x;
			double retSum = retVal[1] - retVal[0];
			if (pointSum < retSum){
				retVal[0] = point.x;
				retVal[1] = point.y;
			}
		}
		return retVal;
	}
}
