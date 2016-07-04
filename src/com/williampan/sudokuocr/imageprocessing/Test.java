package com.williampan.sudokuocr.imageprocessing;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Test {

	public static void main(String[] args) throws TesseractException {
		// TODO Auto-generated method stub
		ImageProcessor ip = new ImageProcessor();
		NumberProcessing np = new NumberProcessing();
		Mat image = ip.processImage();
		np.recogniseNumber(np.processNumberContour(image), image);
	}

}
