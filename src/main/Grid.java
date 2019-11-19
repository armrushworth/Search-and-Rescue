package main;
import java.util.ArrayList;

public class Grid {
	private ArrayList<Cell> grid = new ArrayList<Cell>();
	private Cell currentCell;
	
	private int GRID_WIDTH = 7;
	private int GRID_HEIGHT = 6;
	
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
	
	/**
	 * Finds the corresponding cell for a set of provided coordinates.
	 * @param x the x-coordinate of the cell to find
	 * @param y the y-coordinate of the cell to find
	 * @return the corresponding cell for the coordinates provided
	 */
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
	
	public final void setCurrentCell(Cell currentCell) {
		this.currentCell = currentCell;
	}
}
