package com.williampan.sudokuocr.imageprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.sun.jna.Pointer;

import net.sourceforge.lept4j.Pix;
import net.sourceforge.tess4j.ITessAPI.ETEXT_DESC;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;


public class NumberProcessing {
	
	/**
	 * Find singular square contours from image
	 * @param image Image of sudoku
	 * @return
	 */
	public ArrayList<MatOfPoint> processNumberContour(Mat image){
		// List of potential squares
		ArrayList<MatOfPoint> outMat = new ArrayList<MatOfPoint>();
		// Image preproccesing
		Mat processedImage = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.threshold(image, processedImage, 70, 255, Imgproc.THRESH_BINARY); 
		Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGB); 
		
		Highgui.imwrite("beforeNumberContour.png", image);
		// Find all contours
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(processedImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		for (MatOfPoint contour : contours){
			// Find sudoku singular square
			if (Imgproc.contourArea(contour) > 1000){
				outMat.add(contour);
			}
		}
		
		//Sorting output by order or sudoku
		Collections.sort(outMat, new Comparator<MatOfPoint>() {
				@Override
				public int compare(MatOfPoint contour1, MatOfPoint contour2) {
					Rect roi1 = Imgproc.boundingRect(contour1);
					Rect roi2 = Imgproc.boundingRect(contour2);
					// Not same line
					if (Math.abs(roi2.y - roi1.y) > 10){
						// Compare y values
						if (roi2.y > roi1.y){
							return -1;
						}
						return 1;
					}
					// Same line
					else{
						// Compare x values
						if (roi2.x > roi1.x){
							return -1;
						}
						return 1;
					}
				}
		    });	
		
		Highgui.imwrite("afterNumberContour.png", image);
		return outMat;
	}
	
	/**
	 * Takes a list of contours and attempts to recognise the number
	 * @param contours List of contours
	 * @param image Original image
	 * @throws TesseractException
	 */
	public ArrayList<Integer> recogniseNumber(ArrayList<MatOfPoint> contours, Mat image) throws TesseractException{
		// Sudoku list
		ArrayList<Integer> sudokuOut = new ArrayList<Integer>();
		// Setting up Tesseract API
		TessAPI1.TessBaseAPI handle = TessAPI1.TessBaseAPICreate();
	    String treiningDataPath = "C:\\Tess4J\\";
	    String lang = "eng";
	    TessAPI1.TessBaseAPISetPageSegMode(handle, TessAPI1.TessPageSegMode.PSM_AUTO_ONLY);
	    TessAPI1.TessBaseAPIInit3(handle, treiningDataPath, lang);
		TessAPI1.TessBaseAPISetVariable(handle, "tessedit_char_whitelist", "123456789");
		
		int contCount = 0;		
		for (MatOfPoint contour : contours){
			// Mask image from contour
			Rect roi = Imgproc.boundingRect(contour);
			Mat mask = Mat.zeros(image.size(), CvType.CV_8U);
			Imgproc.drawContours(mask, contours, contCount, new Scalar(255), Core.FILLED);
	        Mat contourRegion = new Mat();
	        Mat imageROI = new Mat();
	        image.copyTo(imageROI, mask);
	        contourRegion = imageROI.submat(roi);
	        // Process the contour region
			Imgproc.cvtColor(contourRegion, contourRegion, Imgproc.COLOR_BGR2GRAY); 
			Imgproc.adaptiveThreshold(contourRegion, contourRegion, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 101, 1);
			// Crop to remove border lines
			Rect cropROI = new Rect(3,3,contourRegion.width()-6,contourRegion.height()-6);
			contourRegion = contourRegion.submat(cropROI);
			// Convert to file usable for number recognition, Mat is not compatable
			Highgui.imwrite("contourRegion.png", contourRegion);
			Tesseract tess = new Tesseract();
			tess.setLanguage("eng");
			tess.setTessVariable("tessedit_char_whitelist", "123456789");
			String outOCR = tess.doOCR(new File("contourRegion.png"));
			// Add OCR value to sudoku list
			if (outOCR.length() != 0){
				sudokuOut.add(Character.getNumericValue(outOCR.charAt(0)));
			}
			else{
				sudokuOut.add(0);
			}
		    contCount += 1;
		}
		
		return sudokuOut;
	}
	
	public int[][] formatSudokuList(ArrayList<Integer> sudokuList) throws Exception{
		if (sudokuList.size() != 81)
			throw new Exception("Recognition Error");
		int[][] sudokuArray = new int[9][9];
		for (int row = 0; row < 9; row ++){
			int[] rowArray = new int[9];
			for (int col = 0; col < 9; col ++){
				rowArray[col] = sudokuList.get(row*9 + col);
			}
			sudokuArray[row] = rowArray;
		}
		return sudokuArray;
	}
}
