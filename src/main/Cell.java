package main;
import java.awt.Point;
import java.util.*;

public class Cell {
	private Point coordinates;
	private boolean isBlocked = false;
	private ArrayList<Cell> neighbours = new ArrayList<Cell>();
	
	private int f = 0; // cost estimate
	private int g = 0; // cost of the path from the start node to the destination
	private int h = 0; // heuristic that estimates the cost of the cheapest path from the start node to the destination
	private Cell previousCell;
	
	public Cell (int x, int y) {
		coordinates = new Point(x, y);
	}
	
	public final Point getCoordinates() {
		return coordinates;
	}
	
	public final int getF() {
		return f;
	}
	
	public final int getG() {
		return g;
	}
	
	public final int getH() {
		return h;
	}
	
	public final ArrayList<Cell> getNeighbours() {
		return neighbours;
	}
	
	public final Cell getPreviousCell() {
		return previousCell;
	}
	
	public final boolean isBlocked() {
		return isBlocked;
	}
	
	public final void setF(int f) {
		this.f = f;
	}
	
	public final void setG(int g) {
		this.g = g;
	}
	
	public final void setH(int h) {
		this.h = h;
	}
	
	public final void setIsBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}
	
	public final void setNeighbours(ArrayList<Cell> neighbours) {
		this.neighbours = neighbours;
	}
	
	public final void setPreviousCell(Cell previousCell) {
		this.previousCell = previousCell;
	}
	
	public final String toString() {
		return coordinates.x + "," + coordinates.y;
	}
}