package main;

import java.util.ArrayList;

public class Grid {
	private Cell currentCell;
	private ArrayList<Cell> grid = new ArrayList<Cell>();
	private final int GRID_WIDTH = 6;
	private final int GRID_HEIGHT = 6;
	
	public Grid() {
		// create the grid for a specified height and width
		for (int y = 0; y < GRID_HEIGHT; y++) {
			for (int x = 0; x < GRID_WIDTH; x++) {
				grid.add(new Cell(x, y));
			}
		}
		
		// populate the neighbours ArrayList for each cell in the grid
		for (int y = 0; y < GRID_HEIGHT; y++) {
			for (int x = 0; x < GRID_WIDTH; x++) {
				ArrayList<Cell> neighbours = new ArrayList<Cell>();
				if (x != 0) neighbours.add(getCell(x - 1, y));
				if (x != GRID_WIDTH - 1) neighbours.add(getCell(x + 1, y));
				if (y != GRID_HEIGHT - 1) neighbours.add(getCell(x, y + 1));
				if (y != 0) neighbours.add(getCell(x, y - 1));
				getCell(x, y).setNeighbours(neighbours);
			}
		}
		
		// TODO be able to localise automatically
		setCurrentCell(getCell(0, 0));
	}
	
	// finds the corresponding cell for a set of provided coordinates
	public final Cell getCell(int x, int y) {
		for (Cell cell : grid) {
			if (cell.getCoordinates().x == x && cell.getCoordinates().y == y) {
				return cell;
			}
		}
		return null;
	}
	
	public final Cell getCurrentCell() {
		return currentCell;
	}
	
	public final ArrayList<Cell> getGrid() {
		return grid;
	}
	
	public final int getGridHeight() {
		return GRID_HEIGHT;
	}
	
	public final int getGridWidth() {
		return GRID_WIDTH;
	}
	
	public final ArrayList<Cell> getNonUrgentVictims() {
		ArrayList<Cell> nonUrgentVictims = new ArrayList<Cell>();
		for (Cell cell : grid) {
			if (cell.getStatus() == 2) {
				nonUrgentVictims.add(cell);
			}
		}
		return nonUrgentVictims;
	}
	
	public final ArrayList<Cell> getPotentialVictims() {
		ArrayList<Cell> potentialVictims = new ArrayList<Cell>();
		for (Cell cell : grid) {
			if (cell.getStatus() == 1) {
				potentialVictims.add(cell);
			}
		}
		return potentialVictims;
	}
	
	public final void setCurrentCell(Cell currentCell) {
		this.currentCell = currentCell;
	}
}