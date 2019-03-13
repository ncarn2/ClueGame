package clueGame;
/*
 * Jordan Newport
 * Nicholas Carnival
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Math.toIntExact;

public class Board {


	private int MAX_BOARD_SIZE = 50;
	private String LayoutFile;
	private String LegendFile;
	private static final String COMMA = ",";

	private int NumRows = 0;
	private int NumColumns = 0;
	
	private Map<BoardCell, Set<BoardCell>> adjacencyMatrix;
	private Set<BoardCell> visited;
	private Set<BoardCell> targets;
	
	//THE ALMIGHTY 2D ARRAY OF BOARD CELLS!!!!!!!!!!!!!
	private BoardCell[][] boardCellArray;
	//THE END OF THE ALMIGHTY 2D ARRAY OF BOARD CELLS :(

	// variable used for singleton pattern
	private static Board theInstance = new Board();
	// constructor is private to ensure only one can be created
	private Board() {}
	// this method returns the only Board
	public static Board getInstance() {
		return theInstance;
	}
	
	//sets the config file variables
	public void setConfigFiles(String layout, String legend) {
		//test if the layout file is the correct length
		this.LayoutFile = layout;
		this.LegendFile = legend;
	}


	//calls two other methods that throw exceptions for JUnit
	//these methods load the config files into local variables
	public void initialize() {

		try {
			loadRoomConfig();
			loadBoardConfig();
		} catch (BadConfigFormatException e) {
			System.out.println("Unable to initialize the board");
		}

	}
	

	private HashMap<Character, String> legendMap = new HashMap<Character, String>();

	//loads in the legend file data
	public void loadRoomConfig() throws BadConfigFormatException{
		getNumRows();
		getNumColumns();
		if(NumColumns == -1) {
			System.out.println("The Bad Format Has Been Thrown");
			throw new BadConfigFormatException("Bad Columns");
		}
		boardCellArray = new BoardCell[NumRows][NumColumns];
		
		//Get scanner instance
        Scanner scanner = null;
		try {
			scanner = new Scanner(new File(LegendFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
         
        //Set the delimiter used in file
        scanner.useDelimiter(",");

        //finds the length of the file
        int count = 0;
        while (scanner.hasNextLine()) {
            count++;
            scanner.nextLine();
        }

        String[] valueArray = new String[count];
        char legendLetter ;
        String legendRoom = "";
        String legendCardStuff = "";
        String temp2 = "";

        //this is size three because of how the legend must be formatted
        String[] splitArray = new String[3];

        //opens the legend file
        try {
			scanner = new Scanner(new File(LegendFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        //iterates through each line of the legend file
        for(int i = 0; i < count; i ++) {
        	valueArray[i] = scanner.nextLine();
        	splitArray = valueArray[i].split(COMMA);

        	legendLetter = splitArray[0].charAt(0);
        	legendRoom = splitArray[1];
        	legendCardStuff = splitArray[2];
        	
        	legendRoom = legendRoom.trim();
        	legendCardStuff = legendCardStuff.trim();
        	
        	if(!legendCardStuff.contentEquals("Card") && !legendCardStuff.contentEquals("Other")) {
        		throw new BadConfigFormatException("The Cards are Not in your favor");
        	}

        	//places the legend values into a hash map
        	legendMap.put(legendLetter, legendRoom);
        	
        }
         
        scanner.close();	
	}
	

	//loads the map csv file and throws if badly formatted
	public void loadBoardConfig() throws BadConfigFormatException{
		Scanner scanner = null;
		getNumColumns();
		getNumRows();

		//numColumns will be -1 if the columns are formatted improperly
		if(NumColumns == -1) {

			System.out.println("threw bad column format");
			throw new BadConfigFormatException("Bad column format");
		
		//loads the csv if the columns are proper
		} else {

			try {

				scanner = new Scanner(new File(LayoutFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			//gridLine should store every line in the csv file
			String[] gridLine = new String[NumRows];

			//stores the same thing without commas
			String[] cleanedGridLine = new String[NumColumns];

			//goes through every column in the csv file and scans the entire line
			for(int column = 0; column < NumRows; column++) {
				gridLine[column] = scanner.nextLine();
				cleanedGridLine = gridLine[column].split(COMMA);

				//scans each string and removes the commas
				for(int row = 0; row < NumColumns; row++) {
					//create a new board cell at a certain location with its char
					//tests if it is a door
					boardCellArray[column][row] = new BoardCell(column,row);

					//checks that the initial stringis not a door
					//System.out.println(cleanedGridLine[row]);
					if(cleanedGridLine[row].length() == 1 || cleanedGridLine[row].charAt(1) == 'N'){
						boardCellArray[column][row].setInitial(cleanedGridLine[row].charAt(0));

						//checks that the character is actually in the map
						if(!legendMap.containsKey(boardCellArray[column][row].getInitial())) {
							throw new BadConfigFormatException("Error: This Character is not in the Legend: " 
									+ boardCellArray[column][row].getInitial());
						}
						boardCellArray[column][row].setDoorway(false);
						if(cleanedGridLine[row] != "X" && cleanedGridLine[row] != "W") {
							boardCellArray[column][row].setRoom(true);
						}
						//this runs if the initial string is a door, determined if there is more than one char
					} else {

						char doorDirectionLetter = cleanedGridLine[row].charAt(1);
						boardCellArray[column][row].setDoorway(true);

						if(doorDirectionLetter == 'L') {
							boardCellArray[column][row].setDoorDirection(DoorDirection.LEFT);
							boardCellArray[column][row].setInitial(cleanedGridLine[row].charAt(0));
						}

						if(doorDirectionLetter == 'R') {
							boardCellArray[column][row].setDoorDirection(DoorDirection.RIGHT);
							boardCellArray[column][row].setInitial(cleanedGridLine[row].charAt(0));
						}

						if(doorDirectionLetter == 'U') {
							boardCellArray[column][row].setDoorDirection(DoorDirection.UP);
							boardCellArray[column][row].setInitial(cleanedGridLine[row].charAt(0));
						}

						if(doorDirectionLetter == 'D') {
							boardCellArray[column][row].setDoorDirection(DoorDirection.DOWN);
							boardCellArray[column][row].setInitial(cleanedGridLine[row].charAt(0));
						}
					}
				}
			}		
			calcAdjacencies();
		}
	}

	//this contains the legend i.e. 'K, Kitchen'
	public Map<Character, String> getLegend() {
		return legendMap;
	}


	//gets the number of columns in the specified csv file
	public int getNumColumns() {

		Scanner scanner = null;
		int count = 0;
		List<String> csvList = Arrays.asList();
		try {
			scanner = new Scanner(new File(LayoutFile));

			ArrayList<Integer> countArray = new ArrayList<Integer>();
			int index = 0;
			while(scanner.hasNextLine()) {
				
				String nextLine = scanner.nextLine();
				
				csvList = Arrays.asList(nextLine.split(COMMA));
				
				countArray.add(csvList.size());
				index++;
			}
			for(int i = 1; i < countArray.size(); i++) {

				if(countArray.get(i) != countArray.get(i-1)) {
					NumColumns = -1;
					return NumColumns;
				}
			}
		} catch (FileNotFoundException e1) {
		}
		NumColumns = csvList.size();
		return NumColumns;
	}

	//should return 20
	public int getNumRows() {
        //counts file length s
		//counts the number of lines in the text file

		long longArray = new Long(1);
		Path path = Paths.get(LayoutFile);
		try {
			longArray = Files.lines(path).count();
		} catch (IOException e) {
		}		
		NumRows = toIntExact(longArray);
		//end of file length counter

		return NumRows;
	}

	//returns a board cell at (row, column)
	public BoardCell getCellAt(int row, int column) {
		return boardCellArray[row][column];
	}

	//this finds what cells are directly next to a specific cell
	public Set<BoardCell> getAdjList(int x, int y) {

		BoardCell currentCell = new BoardCell(x, y);
		Set<BoardCell> adjList ;
		adjList = adjacencyMatrix.get(currentCell);
		return adjList;
	}

	public void calcAdjacencies() {
		adjacencyMatrix = new HashMap<BoardCell, Set<BoardCell>>();
		for (int i = 0; i < NumRows; i++) {
			for (int j = 0; j < NumColumns; j++) {
				adjacencyMatrix.put(boardCellArray[i][j], new HashSet<BoardCell>());
				if (boardCellArray[i][j].getInitial() == 'W') {
					if (i > 0) {
						if (boardCellArray[i-1][j].getInitial() == 'W' || boardCellArray[i-1][j].getDoorDirection() == DoorDirection.DOWN) {
							adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i-1][j]);
						}
					}
					if (j > 0) {
						if (boardCellArray[i][j-1].getInitial() == 'W' || boardCellArray[i][j-1].getDoorDirection() == DoorDirection.RIGHT) {
							adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i][j-1]);
						}
					}
					if (i < NumRows - 1) {
						if (boardCellArray[i+1][j].getInitial() == 'W' || boardCellArray[i+1][j].getDoorDirection() == DoorDirection.UP) {
							adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i+1][j]);
						}
					}
					if (j < NumColumns - 1) {
						if (boardCellArray[i][j+1].getInitial() == 'W' || boardCellArray[i][j+1].getDoorDirection() == DoorDirection.LEFT) {
							adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i][j+1]);
						}
					}
				} else if (boardCellArray[i][j].isDoorway()) {
					switch (boardCellArray[i][j].getDoorDirection()) {
					case LEFT:
						if (j > 0) {
							if(boardCellArray[i][j-1].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i][j-1]);
							}
						}
						break;
					case UP:
						if (i > 0) {
							if(boardCellArray[i-1][j].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i-1][j]);
							}
						}
						break;
					case RIGHT:
						if (j < NumColumns - 1) {
							if(boardCellArray[i][j+1].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i][j+1]);
							}
						}
						break;
					case DOWN:
						if (i < NumRows - 1) {
							if(boardCellArray[i+1][j].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[i][j]).add(boardCellArray[i+1][j]);
							}
						}
						break;
					default:
						break;
					}
				} else if (boardCellArray[i][j].isRoom()) {
					continue;
				}
//				if(i==3&&j==4) {
//					for (BoardCell b : adjacencyMatrix.get(boardCellArray[i][j])) {
//						System.out.println(b);
//					}
//				}
			}
		}
	}

	// calculates what cells are what distance away
	private void findAllTargets(BoardCell thisCell, int numSteps) {
		Set<BoardCell> adjList = adjacencyMatrix.get(thisCell);
		for (BoardCell adjCell : adjList) {
			if (visited.contains(adjCell)) {
				;
			} else {
				visited.add(adjCell);
				if (numSteps == 1 || adjCell.isDoorway()) {
					targets.add(adjCell);
				} else {
					findAllTargets(adjCell, numSteps - 1);
				}
				visited.remove(adjCell);
			}
		}
	}

	public void calcTargets(int x, int y, int pathLength) {
		BoardCell startCell = boardCellArray[x][y];
		System.out.println("in calcTargets");
		targets.clear();
		visited.clear();
		visited.add(startCell);
		findAllTargets(startCell, pathLength);
	}

	public void calcTargets(BoardCell startCell, int pathLength) {
		System.out.println("in calcTargets");
		targets.clear();
		visited.clear();
		visited.add(startCell);
		findAllTargets(startCell, pathLength);
	}

	public Set<BoardCell> getTargets() {
		Set<BoardCell> targetsSet = new HashSet<BoardCell>();
		return targetsSet;
	}

	public static void main(String[] args) {
		Board board ;
		board = Board.getInstance();
		board.setConfigFiles("data/testsMap.csv", "data/rooms.txt");
		board.initialize();
		
		Set<BoardCell> testList = board.getAdjList(0, 0);
		System.out.println("Test List: " + testList);
		System.out.println("testList size (0,0): " + testList.size());
	}

}
