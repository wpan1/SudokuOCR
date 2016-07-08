package com.williampan.sudokuocr.solver;

import java.util.ArrayList;
import java.util.Collections;

public class SudokuSolver {
	/**
	 * Solve Sudoku recursively.
	 * @param row current row index.
	 * @param col current column index.
	 * @return false if Sudoku was not solved. true if Sudoku is solved.
	 */
	public boolean solve(int[][] cells, int row, int col) {
		// If it has passed through all cells, start quitting
		if (row == 9)
			return true;
	 
		// If this cell is already set(fixed), skip to the next cell
		if (cells[row][col] != 0) {
			if (solve(cells, col == 8? (row + 1): row, (col + 1) % 9))
				return true;
		} else {
			// Random numbers 1 - 9
			Integer[] randoms = generateRandomNumbers();
			for (int i = 0; i < 9; i++) {
				// If no duplicates in this row, column, 3x3, assign the value and go to the next
				if (!containedInRowCol(cells, row, col, randoms[i]) && 
						!containedIn3x3Box(cells, row, col, randoms[i])) {
					cells[row][col] = randoms[i];
	 
					// Move to the next cell left-to-right and top-to-bottom
					if (solve(cells, col == 8? (row + 1) : row, (col + 1) % 9))
						return true;
					else { // Initialize the cell when backtracking (case when the value in the next cell was not valid)
						cells[row][col] = 0;
					}
				}
			}
		}
	 
		return false;
	}
	
	/**
	 * Check if a value contains in its 3x3 box for a cell.
	 * @param row current row index.
	 * @param col current column index.
	 * @return true if this cell is incorrect or duplicated in its 3x3 box.
	 */
	private boolean containedIn3x3Box(int[][] cells, int row, int col, int value) {
		// Find the top left of its 3x3 box to start validating from
		int startRow = row / 3 * 3;
		int startCol = col / 3 * 3;
	 
		// Check within its 3x3 box except its cell
		for (int i = startRow; i < startRow + 3; i++)
			for (int j = startCol; j < startCol + 3; j++) {
				if (!(i == row && j == col)) {
					if (cells[i][j] == value){
						return true;
					}
				}
			}
	 
		return false;
	}

	/**
	 * Check if a value is contained within its row and column.
	 * Used when solving the puzzle.
	 * @param row current row index.
	 * @param col current column index.
	 * @param value value in this cell.
	 * @return true if this value is duplicated in its row and column.
	 */
	private boolean containedInRowCol(int[][] cells, int row, int col, int value) {
		for (int i = 0; i < 9; i++) {
			// Don't check the same cell
			if (i != col)
				if (cells[row][i] == value)
					return true;
			if (i != row)
				if (cells[i][col] == value)
					return true;
		}
	 
		return false;
	}
	
	/**
	 * Generate 9 unique random numbers.
	 * @return array containing 9 random unique numbers.
	 */
	private Integer[] generateRandomNumbers() {
		ArrayList<Integer> randoms = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++)
			randoms.add(i + 1);
		Collections.shuffle(randoms);
	 
		return randoms.toArray(new Integer[9]);
	}
	
	public void printBoard(int[][] sudoku) {
	    for (int i = 0; i < 9; i++) {
	            System.out.print("\n");
	            if(i%3==0)
	                System.out.print("\n");
	        for (int j = 0; j < 9; j++) {
	            if (j % 3 == 0)
	                System.out.print(" ");
	            if (sudoku[i][j] == 0)
	                System.out.print(". ");
	            if (sudoku[i][j] == 1)
	                System.out.print("1 ");
	            if (sudoku[i][j] == 2)
	                System.out.print("2 ");
	            if (sudoku[i][j] == 3)
	                System.out.print("3 ");
	            if (sudoku[i][j] == 4)
	                System.out.print("4 ");
	            if (sudoku[i][j] == 5)
	                System.out.print("5 ");
	            if (sudoku[i][j] == 6)
	                System.out.print("6 ");
	            if (sudoku[i][j] == 7)
	                System.out.print("7 ");
	            if (sudoku[i][j] == 8)
	                System.out.print("8 ");
	            if (sudoku[i][j] == 9)
	                System.out.print("9 ");
	        }
	    }
	}
}
