package com.williampan.sudokuocr.imageprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ImageProcessor {
	
	/**
	 * Process sudoku puzzle
	 */
	public void processImage(){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// Read in sudoku image file
		File file = new File("cbhsudoku.jpg");
		Mat image = Highgui.imread(file.getAbsolutePath());
		Mat processedImage = preprocessImage(image);
		MatOfPoint contourBoundary = findBoundary(processedImage);
		Mat maskedImage = maskContour(contourBoundary, image);
		squashContour(contourBoundary, image);
		// Image preproccesing
		Mat vertImage = Mat.zeros(maskedImage.size(), CvType.CV_8UC3);
		findVertical(maskedImage, vertImage);
		Mat horImage = Mat.zeros(maskedImage.size(), CvType.CV_8UC3);
		findHorizontal(maskedImage, horImage);
		Mat andImage = Mat.zeros(maskedImage.size(), CvType.CV_8UC3);
		Core.bitwise_and(vertImage, horImage, andImage);
		Highgui.imwrite("afterBitAnd.png", andImage);
		findCentroids(andImage, maskedImage);
	}
	
	/**
	 * Preprocess the image using blur and thresholding
	 * @param image Input/Output image
	 */
	private Mat preprocessImage(Mat image){
		Mat imageA = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		// Convert to grayscale
		Imgproc.cvtColor(image, imageA, Imgproc.COLOR_BGR2GRAY);
		// Guassian blur
		Imgproc.GaussianBlur(imageA, imageA, new Size(5,5), 0);
		// Thresholding
		Imgproc.adaptiveThreshold(imageA, imageA, 255, 0, 1, 21, 2);
		// Output file
		Highgui.imwrite("afterPreprocess.png", imageA);
		return imageA;
	}
	
	/**
	 * Find the boundary of the sudoku puzzle, approximate as polygon
	 * @param proccessedImage image to process
	 */
	private MatOfPoint findBoundary(Mat proccessedImage){
		// Find all rectangles in image
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(proccessedImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		// Find largest rectangle, which should be the sudoku puzzle
		double largestCountourArea = -1;
		int contourIndex = 0;
		for (int i=0; i < contours.size(); i++){
			double area = Imgproc.contourArea(contours.get(i));
			if (area > largestCountourArea){
				contourIndex = i;
				largestCountourArea = area;
			}
		}
		
		return contours.get(contourIndex);
	}
	
	/**
	 * Squash contour/image into a 400x400 square box
	 * This is needed for number OCR as OCR requires fixed number sizes
	 * @param contour Matrix of contour
	 * @param image Image to modify
	 */
	private void squashContour(MatOfPoint contour, Mat image){
		// Convert to matofpoint2f sice polygon approximation does not work
		// with matofpoint
		MatOfPoint boundaryContour = contour;
		MatOfPoint2f convertedContour = new MatOfPoint2f();
		boundaryContour.convertTo(convertedContour, CvType.CV_32FC2);
		
		// Approximate polygon and modify contour
		Double peri = Imgproc.arcLength(convertedContour, true);
		Imgproc.approxPolyDP(convertedContour, convertedContour, 0.02*peri, true);
		convertedContour.convertTo(contour, CvType.CV_32S);
		
		// Process corners of contour
		CornerProcessor cornerProcessor = new CornerProcessor();
		Point[] points = contour.toArray();
		double[] topLeft = cornerProcessor.topLeftPoint(points);
		double[] topRight = cornerProcessor.topRightPoint(points);
		double[] botLeft = cornerProcessor.botLeftPoint(points);
		double[] botRight = cornerProcessor.botRightPoint(points);
		// Create matrices of corner points of contour and corners to transform to
		double[][] cornerSrcPoints = {topLeft, topRight, botLeft, botRight};
		double[][] cornerDestTrans = {{0,0}, {399,0}, {0,399}, {399,399}}; 
		Mat cornerSrcMat = new Mat(4, 2, CvType.CV_32F);
		Mat cornerTransMat = new Mat(4, 2, CvType.CV_32F);
		for(int row=0; row<4; row++){
			   for(int col=0; col<2; col++){
				   cornerSrcMat.put(row, col, cornerSrcPoints[row][col]);
				   cornerTransMat.put(row, col, cornerDestTrans[row][col]);
			   }
			}
		// Transform into 400x400 square
		Mat transformMatrix = Imgproc.getPerspectiveTransform(cornerSrcMat, cornerTransMat);
		Imgproc.warpPerspective(image, image, transformMatrix, new Size(400,400));
		Highgui.imwrite("afterSquash.png", image);
	}
	
	/**
	 * Squash contour/image into a 400x400 square box
	 * This is needed for number OCR as OCR requires fixed number sizes
	 * @param contour Matrix of contour
	 * @param image Image to modify
	 */
	private Mat maskContour(MatOfPoint contour, Mat image){
		Mat mask = Mat.zeros(image.rows(), image.cols(), CvType.CV_8UC3);
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		contours.add(contour);
		Imgproc.drawContours(mask, contours, 0, new Scalar(255,255,255), Core.FILLED);
		
		Mat crop = new Mat(image.rows(), image.cols(), CvType.CV_8UC3);
		crop.setTo(new Scalar(0,0,0));
		image.copyTo(crop, mask);
		Core.normalize(mask.clone(), mask, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1);
		
		return crop;
	}
	
	/**
	 * Find vertical lines from contour image
	 * @param image Image with contour
	 */
	private void findVertical(Mat image, Mat lineImage){
		// Image preproccesing
		Mat processedImage = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2GRAY);
		// Guassian blur
		//Imgproc.GaussianBlur(processedImage, processedImage, new Size(5,5), 0);	
		
		// Kernel to morph line thickness
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,10));
		// Process image so that vertical lines can be found
		Highgui.imwrite("beforesobel.png", processedImage);
		Imgproc.Sobel(processedImage, processedImage, CvType.CV_16S, 1, 0);
		Core.convertScaleAbs(processedImage, processedImage);
		Core.normalize(processedImage, processedImage, 0, 255, Core.NORM_MINMAX);
		Highgui.imwrite("beforethresh.png", processedImage);
		Imgproc.threshold(processedImage, processedImage, 0, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
		Highgui.imwrite("beforemorph.png", processedImage);
		Imgproc.morphologyEx(processedImage, processedImage, Imgproc.MORPH_DILATE, kernel);			
		Highgui.imwrite("beforeVertical2.png", processedImage);
		
		// Find all contours in processedprocessedImage
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(processedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		// Find contours that could be possible vertical lines
		int contourID = 0;
		for (MatOfPoint contour : contours){
			Rect rect = Imgproc.boundingRect(contour);
			if (rect.height/rect.width > 5 && rect.height > 200){
				Imgproc.drawContours(lineImage, contours, contourID, new Scalar(0,255,-1),-1);
			}
			else{
			}
			contourID ++;
		}
	
		Mat kernelzero = Mat.zeros(2, 10, CvType.CV_8U);
		//Imgproc.morphologyEx(lineImage, lineImage, Imgproc.MORPH_CLOSE, kernelzero);
		Highgui.imwrite("afterVertical.png", lineImage);
	}
	
	/**
	 * Find vertical lines from contour image
	 * @param image Image with contour
	 */
	private void findHorizontal(Mat image, Mat lineImage){
		// Image preproccesing
		Mat processedImage = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2GRAY);
		// Guassian blur
		//Imgproc.GaussianBlur(processedImage, processedImage, new Size(5,5), 0);	
		
		// Kernel to morph line thickness
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10,2));
		// Process image so that vertical lines can be found.
		Highgui.imwrite("beforehorizontalsobel.png", processedImage);
		Imgproc.Sobel(processedImage, processedImage, CvType.CV_8U, 0, 2);
		Highgui.imwrite("afterhorizontalsobel.png", processedImage);
		Core.convertScaleAbs(processedImage, processedImage);
		Core.normalize(processedImage, processedImage, 0, 255, Core.NORM_MINMAX);
		Imgproc.threshold(processedImage, processedImage, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
		Highgui.imwrite("beforehorizontalmorph.png", processedImage);
		Imgproc.morphologyEx(processedImage, processedImage, Imgproc.MORPH_DILATE, kernel);
		Highgui.imwrite("afterhorizontalmorph.png", processedImage);
		
		// Find all contours in processedprocessedImage
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(processedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		// Find contours that could be possible vertical lines
		int contourID = 0;
		for (MatOfPoint contour : contours){
			Rect rect = Imgproc.boundingRect(contour);
			if (rect.width/rect.height > 5 && rect.width > 200){
				Imgproc.drawContours(lineImage, contours, contourID, new Scalar(0,255,-1),-1);
			}
			else{
				//Imgproc.drawContours(image, contours, contourID, new Scalar(0,0,-1),-1);
			}
			contourID ++;
		}
	
		Mat kernelzero = Mat.zeros(2, 10, CvType.CV_8U);
		//Imgproc.morphologyEx(lineImage, lineImage, Imgproc.MORPH_CLOSE, kernelzero);
		Highgui.imwrite("afterHoriztonal.png", lineImage);
	}
	
	/**
	 * Generatre centroids at intersections of sudoku lines
	 * @param image Image of and between horizontal and vertical lines
	 * @param originalImage Image to draw centroids onto
	 * @return ArrayList of centroids
	 */
	private ArrayList<Point> findCentroids(Mat image, Mat originalImage){
		// Image preproccesing
		Mat processedImage = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.cvtColor(image, processedImage, Imgproc.COLOR_BGR2GRAY);
		// Morph inconsistencies
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5));
		Imgproc.morphologyEx(processedImage, processedImage, Imgproc.MORPH_DILATE, kernel);
		Highgui.imwrite("afterCentroidMorph.png", processedImage);
		// Find all contours in Image
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(processedImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		ArrayList<Point> centroids = new ArrayList<Point>();
		for (MatOfPoint contour : contours){
			Moments moment = Imgproc.moments(contour);
			int x = (int)(moment.get_m10()/moment.get_m00());
			int y = (int)(moment.get_m01()/moment.get_m00());
			Point circleCentre = new Point(x,y);
			Core.circle(originalImage, circleCentre, 4, new Scalar(0,255,0));
			centroids.add(circleCentre);
		}
		
		//Sorting centroids by order
		Collections.sort(centroids, new Comparator<Point>() {
				@Override
				public int compare(Point point1, Point point2) {
					// Not same line
					if (Math.abs(point2.y - point1.y) > 10){
						// Compare y values
						if (point2.y > point1.y){
							return -1;
						}
						return 1;
					}
					// Same line
					else{
						// Compare x values
						if (point2.x > point1.x){
							return -1;
						}
						return 1;
					}
				}
		    });	
		
		// Display Centroids on image
		int count = 0;	
		for (Point point : centroids){
			System.out.println(count + ": " + point.x + "," + point.y);
			Core.putText(originalImage, String.valueOf(count), point, Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(0,0,255));
			count += 1;
		}
		Highgui.imwrite("afterCentroids.png", originalImage);
		
		return centroids;
	}
	
}
