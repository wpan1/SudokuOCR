package com.williampan.sudokuocr.imageprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
	
	public ArrayList<MatOfPoint> processNumberContour(Mat image){
		ArrayList<MatOfPoint> outMat = new ArrayList<MatOfPoint>();
		// Image preproccesing
		Mat processedImage = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.threshold(image, processedImage, 70, 255, Imgproc.THRESH_BINARY); 
		
		Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGB); 
		
		Highgui.imwrite("beforeNumberContour.png", image);
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(processedImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		int count = 0;
		for (MatOfPoint contour : contours){
			// Find sudoku singular square
			if (Imgproc.contourArea(contour) > 1000){
				outMat.add(contour);
			}
			count ++ ;
		}
		System.out.println(outMat.size());
		Highgui.imwrite("afterNumberContour.png", image);
		return outMat;
	}
	
	public void recogniseNumber(ArrayList<MatOfPoint> contours, Mat image) throws TesseractException{
		TessAPI1.TessBaseAPI handle = TessAPI1.TessBaseAPICreate();
	    String treiningDataPath = "C:\\Tess4J\\";
	    String lang = "eng";
	    TessAPI1.TessBaseAPISetPageSegMode(handle, TessAPI1.TessPageSegMode.PSM_AUTO_ONLY);
	    TessAPI1.TessBaseAPIInit3(handle, treiningDataPath, lang);
		TessAPI1.TessBaseAPISetVariable(handle, "tessedit_char_whitelist", "123456789");
		
		int contCount = 0;		
		for (MatOfPoint contour : contours){
			Rect roi = Imgproc.boundingRect(contour);
			Mat mask = Mat.zeros(image.size(), CvType.CV_8U);
			Imgproc.drawContours(mask, contours, contCount, new Scalar(255), Core.FILLED);
	        Mat contourRegion = new Mat();
	        Mat imageROI = new Mat();
	        image.copyTo(imageROI, mask);
	        contourRegion = imageROI.submat(roi);
			Imgproc.cvtColor(contourRegion, contourRegion, Imgproc.COLOR_BGR2GRAY); 
			Imgproc.adaptiveThreshold(contourRegion, contourRegion, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 101, 1);
			
			Rect cropROI = new Rect(3,3,contourRegion.width()-6,contourRegion.height()-6);
			contourRegion = contourRegion.submat(cropROI);
			
			Highgui.imwrite("contourRegion" + contCount + ".png", contourRegion);
			Tesseract tess = new Tesseract();
			tess.setLanguage("eng");
			tess.setTessVariable("tessedit_char_whitelist", "123456789");
			recogniseNumber(tess.doOCR(new File("contourRegion" + contCount + ".png")));
	        		
//			byte[] bytes = new byte[contourRegion.channels() * contourRegion.cols() * contourRegion.rows()];
//			contourRegion.get(0, 0, bytes);
//			BufferedImage img = new BufferedImage(contourRegion.cols(), contourRegion.rows(), BufferedImage.TYPE_3BYTE_BGR);
//			final byte[] imageInBytes = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
//			System.arraycopy(bytes, 0, imageInBytes, 0, bytes.length);
//			
//			TessAPI1.TessBaseAPISetImage(handle, ByteBuffer.wrap(imageInBytes), (int)contourRegion.width(), (int)contourRegion.height(), contourRegion.channels(), (int)contourRegion.step1());
//			TessAPI1.TessBaseAPIRecognize(handle, null);
//			Pointer out = TessAPI1.TessBaseAPIGetUTF8Text(handle);
//			String tempStr = out.getString(0);
//		    recogniseNumber(tempStr);
		    
		    contCount += 1;
		}
	}

	private void recogniseNumber(String tempStr) {
		if (tempStr.length() != 0){
			char tempChar = tempStr.charAt(0);
		    if(tempChar=='1')
		        System.out.println(1);
		    else if(tempChar=='2')
		        System.out.println(2);
		    else if(tempChar=='3')
		        System.out.println(3);
		    else if(tempChar=='4')
		        System.out.println(4);
		    else if(tempChar=='5')
		        System.out.println(5);
		    else if(tempChar=='6')
		        System.out.println(6);
		    else if(tempChar=='7')
		        System.out.println(7);
		    else if(tempChar=='8')
		        System.out.println(8);
		    else if(tempChar=='9')
		        System.out.println(9);
//		    else
//		        System.out.println(0);
		}
//		else
//		    System.out.println(0);
	}
}
