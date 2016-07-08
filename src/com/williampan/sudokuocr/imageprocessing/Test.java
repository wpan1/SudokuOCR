package com.williampan.sudokuocr.imageprocessing;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import com.williampan.sudokuocr.solver.SudokuSolver;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ImageProcessor ip = new ImageProcessor();
		NumberProcessing np = new NumberProcessing();
		SudokuSolver ss = new SudokuSolver();
		Mat image = ip.processImage();
		int[][] cells = np.formatSudokuList(np.recogniseNumber(np.processNumberContour(image), image));
		ss.printBoard(cells);
	}

}
