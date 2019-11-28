package main;

import java.util.ArrayList;

public class HungarianMethod {
	private Grid grid;
	private PathFinder pathFinder;
	private ArrayList<Cell> potentialVictims;
	private ArrayList<Cell> locations;
	private ArrayList<ArrayList<Integer>> dataMatrix = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> graph;
	private ArrayList<Cell> route = new ArrayList<Cell>();
	
	public HungarianMethod(Grid grid, ArrayList<Cell> potentialVictims) {
		this.grid = grid;
		pathFinder = new PathFinder(grid.getGrid());
		this.potentialVictims = potentialVictims;
		this.locations = new ArrayList<Cell>(potentialVictims);
		locations.add(0, grid.getCurrentCell());
		
		dataMatrix = createDataMatrix();
		graph = new ArrayList<>(locations.size());
		for (int i = 0; i < locations.size(); i++) {
			graph.add(new ArrayList<Integer>());
		}
	}
	
	public final ArrayList<Cell> findRoute() {
		for (int i = 0; i < potentialVictims.size(); i++) {
			subtractLowestRowValue();
			subtractLowestColumnValue();
			findHighestPenalty();
		}
		
		route.add(locations.get(0));
		int index = 0;
		while (route.size() < locations.size()) {
			index = graph.get(index).get(0);
			route.add(locations.get(index));
		}
		route.remove(0);
		return route;
	}
	
	private final ArrayList<ArrayList<Integer>> createDataMatrix() {
		ArrayList<ArrayList<Integer>> dataMatrixBuilder = new ArrayList<ArrayList<Integer>>();
		for (Cell location : locations) {
			ArrayList<Integer> row = new ArrayList<Integer>();
			for (Cell destination : locations) {
				if (!location.isBlocked()) {
					if (location != destination) {
						ArrayList<Cell> path = new ArrayList<Cell>();
						path = pathFinder.findPath(path, location, destination);
						
						// add distance between cells to matrix row
						if (path != null) {
							row.add(destination.getG());
							
						// set cell as blocked and remove from potential victims if unreachable
						} else {
							destination.setIsBlocked();
							potentialVictims.remove(destination);
						}
						
					// add -1 if location and destination are the same
					} else {
						row.add(-1);
					}
				}
			}
			
			// add row to matrix if cell is reachable
			if (!row.isEmpty()) {
				dataMatrixBuilder.add(row);
			}
		}
		return dataMatrixBuilder;
	}
	
	private final void subtractLowestRowValue() {
		for (int i = 0; i < dataMatrix.size(); i++) {
			int min = Integer.MAX_VALUE;
			for (int j = 0; j < dataMatrix.size(); j++) {
				int value = dataMatrix.get(i).get(j);
				if (value != -1 && value < min) {
					min = value;
				}
			}
			
			for (int j = 0; j < dataMatrix.size(); j++) {
				int value = dataMatrix.get(i).get(j);
				if (value != -1) {
					dataMatrix.get(i).set(j, value - min);
				}
			}
		}
	}
	
	private final void subtractLowestColumnValue() {
		for (int i = 0; i < dataMatrix.size(); i++) {
			int min = Integer.MAX_VALUE;
			for (int j = 0; j < dataMatrix.size(); j++) {
				int value = dataMatrix.get(j).get(i);
				if (value != -1 && value < min) {
					min = value;
				}
			}
			
			for (int j = 0; j < dataMatrix.size(); j++) {
				int value = dataMatrix.get(j).get(i);
				if (value != -1) {
					dataMatrix.get(j).set(i, value - min);
				}
			}
		}
	}
	
	private final void findHighestPenalty() {
		int[] highestPenalty = new int[3]; // 0 - location, 1 - destination, 2 - value
		for (int i = 0; i < dataMatrix.size(); i++) {
			for (int j = 0; j < dataMatrix.size(); j++) {
				if (dataMatrix.get(i).get(j) == 0) {
					int lowestHouseValue = findLowestHouseValue(i, j);
					if (lowestHouseValue >= highestPenalty[2] && j != 0 && !isCyclic(i, j)) {
						highestPenalty[0] = i;
						highestPenalty[1] = j;
						highestPenalty[2] = lowestHouseValue;
					}
				}
			}
		}
		
		dataMatrix.get(highestPenalty[1]).set(highestPenalty[0], -1);
		for (int i = 0; i < dataMatrix.size(); i++) {
			dataMatrix.get(highestPenalty[0]).set(i, -1);
			dataMatrix.get(i).set(highestPenalty[1], -1);
		}
		graph.get(highestPenalty[0]).add(highestPenalty[1]);
	}
	
	private final boolean isCyclicUtil(int i, boolean[] visited, boolean[] recStack, ArrayList<ArrayList<Integer>> testGraph) { 
		if (recStack[i]) {
			return true;
		}
		if (visited[i]) {
			return false;
		}
		visited[i] = true;
		recStack[i] = true;
		ArrayList<Integer> children = testGraph.get(i);
		for (Integer c: children)
			if (isCyclicUtil(c, visited, recStack, testGraph)) {
				return true;
			}
		recStack[i] = false;
		return false;
	}

	private final boolean isCyclic(int source, int destination) {
		ArrayList<ArrayList<Integer>> testGraph = new ArrayList<>(locations.size());
		for (ArrayList<Integer> edge : graph) {
			testGraph.add(new ArrayList<Integer>(edge));
		}
		testGraph.get(source).add(destination);
		boolean[] visited = new boolean[locations.size()];
		boolean[] recStack = new boolean[locations.size()];
		for (int i = 0; i < locations.size(); i++) {
			if (isCyclicUtil(i, visited, recStack, testGraph)) {
				return true;
			}
		}
		return false;
	}
	
	private final int findLowestHouseValue(int row, int column) {
		int lowestRowValue = Integer.MAX_VALUE;
		for (int i = 0; i < dataMatrix.size(); i++) {
			if (i != column) {
				int value = dataMatrix.get(row).get(i);
				if (value != -1 && value < lowestRowValue) {
					lowestRowValue = value;
				}
			}
		}
		if (lowestRowValue == Integer.MAX_VALUE) {
			lowestRowValue = 0;
		}
		
		int lowestColumnValue = Integer.MAX_VALUE;
		for (int i = 0; i < dataMatrix.size(); i++) {
			if (i != row) {
				int value = dataMatrix.get(i).get(column);
				if (value != -1 && value < lowestColumnValue) {
					lowestColumnValue = value;
				}
			}
		}
		if (lowestColumnValue == Integer.MAX_VALUE) {
			lowestColumnValue = 0;
		}
		
		return lowestRowValue + lowestColumnValue;
	}
	
	private final void printDataMatrix() {
		String row = "   ";
		for (int i = 0; i < dataMatrix.size(); i++) {
			row += String.format("%1$" + 4 + "s", i == 0 ? grid.getCurrentCell().toString() : potentialVictims.get(i - 1).toString());
		}
		System.out.println(row);
		
		int locationCounter = -1;
		for (ArrayList<Integer> location : dataMatrix) {
			row = (locationCounter == -1 ? grid.getCurrentCell().toString() : potentialVictims.get(locationCounter).toString());
			for (int destination : location) {
				row += String.format("%1$" + 4 + "s", destination).replaceAll("-1", " -");
			}
			System.out.println(row);
			locationCounter++;
		}
		System.out.println();
	}
	
	private final void printRoute() {
		String routeString = "Route: ";
		String pathString = "\nFull Path: " + route.get(0) + " -> ";
		for (int i = 0; i < route.size(); i++) {
			routeString += route.get(i).toString() + " -> ";
			if (i != route.size() - 1) {
				ArrayList<Cell> path = new ArrayList<Cell>();
				path = pathFinder.findPath(path, route.get(i), route.get(i + 1));
				for (Cell cell : path) {
					pathString += cell.toString() + " -> ";
				}
			}
		}
		System.out.println(routeString.substring(0, routeString.length() - 4));
		System.out.println(pathString.substring(0, pathString.length() - 4));
	}
}
