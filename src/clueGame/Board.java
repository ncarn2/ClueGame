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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Board {


	public static final int MAX_BOARD_SIZE = 50;
	private String LayoutFile;
	private String LegendFile;
	private static final String COMMA = ",";

	private int NumRows = 0;
	private int NumColumns = 0;
	
	private Map<BoardCell, Set<BoardCell>> adjacencyMatrix;
	private Set<BoardCell> visited;
	private Set<BoardCell> targets;

	private HashMap<Character, String> legendMap = new HashMap<Character, String>();
	
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
			getNumRows();
			getNumColumns();

			loadRoomConfig();
			loadBoardConfig();
		} catch (BadConfigFormatException e) {
			System.out.println("Unable to initialize the board");
		}

	}
	

	//loads in the legend file data
	public void loadRoomConfig() throws BadConfigFormatException{
		//these two functions write to NumRows and NumColumns variables
		//-1 is an error state for NumColumns
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
        scanner.useDelimiter(COMMA);

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

        //this is size three because of how the legend must be formatted
        String[] splitArray = new String[3];

        //opens the legend file
        try {
			scanner = new Scanner(new File(LegendFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        //iterates through each line of the legend file and adds it to the legendMap
        for(int i = 0; i < count; i ++) {
        	valueArray[i] = scanner.nextLine();
        	splitArray = valueArray[i].split(COMMA);

        	legendLetter = splitArray[0].charAt(0);
        	legendRoom = splitArray[1];
        	legendCardStuff = splitArray[2];
        	
        	legendRoom = legendRoom.trim();
        	legendCardStuff = legendCardStuff.trim();
        	
        	//if the legend has something that is neither a regular room nor a walkway/closet
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
		if(NumColumns > MAX_BOARD_SIZE || NumRows > MAX_BOARD_SIZE) {
			throw new BadConfigFormatException("Board size exceeds max board size of "
				+ MAX_BOARD_SIZE + " in at least one dimension");
		}

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

			//stores a line of the file without commas
			String[] cleanedGridLine = new String[NumColumns];

			//goes through every column in the csv file and scans the entire line
			for(int row = 0; row < NumRows; row++) {
				gridLine[row] = scanner.nextLine();
				cleanedGridLine = gridLine[row].split(COMMA);

				//scans each string and removes the commas
				for(int column = 0; column < NumColumns; column++) {
					//create a new board cell at a certain location with its char
					boardCellArray[row][column] = new BoardCell(row,column);

					//checks that the initial string is not a door
					//i.e. if there is not more than one char or the second char is N
					if(cleanedGridLine[column].length() == 1 || cleanedGridLine[column].charAt(1) == 'N'){
						//load in initial character for board cell
						boardCellArray[row][column].setInitial(cleanedGridLine[column].charAt(0));

						//checks that the character is actually in the map
						if(!legendMap.containsKey(boardCellArray[row][column].getInitial())) {
							throw new BadConfigFormatException("Error: This Character is not in the Legend: "
									+ boardCellArray[row][column].getInitial());
						}
						boardCellArray[row][column].setDoorway(false);
						//everything other than a walkway or closet is a room
						if(cleanedGridLine[column] != "X" && cleanedGridLine[column] != "W") {
							boardCellArray[row][column].setRoom(true);
						}
					//this runs if the initial string is a door
					} else {
						//load in initial character for board cell
						boardCellArray[row][column].setInitial(cleanedGridLine[column].charAt(0));

						//set up which direction the door is facing
						char doorDirectionLetter = cleanedGridLine[column].charAt(1);
						boardCellArray[row][column].setDoorway(true);

						switch(doorDirectionLetter) {
						case 'L':
							boardCellArray[row][column].setDoorDirection(DoorDirection.LEFT);
							break;
						case 'R':
							boardCellArray[row][column].setDoorDirection(DoorDirection.RIGHT);
							break;
						case 'U':
							boardCellArray[row][column].setDoorDirection(DoorDirection.UP);
							break;
						case 'D':
							boardCellArray[row][column].setDoorDirection(DoorDirection.DOWN);
							break;
						default:
							throw new BadConfigFormatException("There is a cell whose second letter is "
									+ doorDirectionLetter);
						}
					}
				}
			}		
			//now that we have all our board cells, we can calculate each of their AdjacencyLists
			calcAdjacencies();
		}
	}

	//this contains the legend e.g. 'K, Kitchen'
	public Map<Character, String> getLegend() {
		return legendMap;
	}


	//gets the number of columns in the specified csv file
	public int getNumColumns() {

		Scanner scanner = null;
		List<String> csvList = Arrays.asList();
		try {
			scanner = new Scanner(new File(LayoutFile));

			ArrayList<Integer> countArray = new ArrayList<Integer>();
			//go through the layout file, taking note of how long each line is
			while(scanner.hasNextLine()) {
				
				String nextLine = scanner.nextLine();
				csvList = Arrays.asList(nextLine.split(COMMA));
				//each line, keep track of how many columns are in it
				countArray.add(csvList.size());
			}
			/*
			if there is ever a different number of columns in two lines,
			then we should know and give back -1 as a failure state

			this is not handled as an exception because
			this method is called in places where it may not throw
			*/
			for(int i = 1; i < countArray.size(); i++) {
				if(countArray.get(i) != countArray.get(i-1)) {
					NumColumns = -1;
					return NumColumns;
				}
			}
		} catch (FileNotFoundException e) {
			//not really anything better to do with this
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		NumColumns = csvList.size();
		return NumColumns;
	}

	//counts the number of lines in the text file
	public int getNumRows() {

		//to be honest we're not sure what we did here and we need to refactor
		//probably it counts the lines, but it seems like there should be a simpler way than this one
		long longArray = new Long(1);
		Path path = Paths.get(LayoutFile);
		try {
			longArray = Files.lines(path).count();
		} catch (IOException e) {
		}		
		NumRows = toIntExact(longArray);
		return NumRows;
	}

	//returns the board cell at (row, column)
	public BoardCell getCellAt(int row, int column) {
		return boardCellArray[row][column];
	}

	//this returns the set of cells which are directly next to a specific cell as referenced by its coordinates
	public Set<BoardCell> getAdjList(int x, int y) {

		BoardCell currentCell = new BoardCell(x, y);
		Set<BoardCell> adjList ;
		adjList = adjacencyMatrix.get(currentCell);
		return adjList;
	}

	//calculates the boards cell adjacencies
	public void calcAdjacencies() {
		//creates a hash map to store each cells set of adjacencies
		adjacencyMatrix = new HashMap<BoardCell, Set<BoardCell>>();
		//for every row
		for (int row = 0; row < NumRows; row++) {
			//for every column
			for (int column = 0; column < NumColumns; column++) {
				//put the current cell into the key of the hash map
				adjacencyMatrix.put(boardCellArray[row][column], new HashSet<BoardCell>());
				//if this cell is a walkway we will check its adjacencies this way:
				//all adjacent walkways, or any doors that are facing the correct direction
				//it does check if a cell is in bounds first--to avoid null pointer
				if (boardCellArray[row][column].getInitial() == 'W') {
					if (row > 0) {
						if (boardCellArray[row-1][column].getInitial() == 'W' ||
								boardCellArray[row-1][column].getDoorDirection() == DoorDirection.DOWN) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row-1][column]);
						}
					}
					if (column > 0) {
						if (boardCellArray[row][column-1].getInitial() == 'W' ||
								boardCellArray[row][column-1].getDoorDirection() == DoorDirection.RIGHT) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column-1]);
						}
					}
					if (row < NumRows - 1) {
						if (boardCellArray[row+1][column].getInitial() == 'W' ||
								boardCellArray[row+1][column].getDoorDirection() == DoorDirection.UP) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row+1][column]);
						}
					}
					if (column < NumColumns - 1) {
						if (boardCellArray[row][column+1].getInitial() == 'W' ||
								boardCellArray[row][column+1].getDoorDirection() == DoorDirection.LEFT) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column+1]);
						}
					}
				//otherwise if this cell is a doorway, then the only adjacency will be the walkway right outside
				} else if (boardCellArray[row][column].isDoorway()) {
					switch (boardCellArray[row][column].getDoorDirection()) {
					case LEFT:
						if (column > 0) {
							if(boardCellArray[row][column-1].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column-1]);
							}
						}
						break;
					case UP:
						if (row > 0) {
							if(boardCellArray[row-1][column].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row-1][column]);
							}
						}
						break;
					case RIGHT:
						if (column < NumColumns - 1) {
							if(boardCellArray[row][column+1].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column+1]);
							}
						}
						break;
					case DOWN:
						if (row < NumRows - 1) {
							if(boardCellArray[row+1][column].getInitial() == 'W') {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row+1][column]);
							}
						}
						break;
					default:
						break;
					}
				} else if (boardCellArray[row][column].isRoom()) {
					//rooms have empty adjacencies
					continue;
				}
			}
		}
	}

	// calculates what cells are what distance away
	//this is based directly on the pseudocode in the presentation
	private void findAllTargets(BoardCell thisCell, int numSteps) {
		Set<BoardCell> adjList = adjacencyMatrix.get(thisCell);
		for (BoardCell adjCell : adjList) {
			//if has already visited this cell
			if (visited.contains(adjCell)) {
				continue;
			} else {
				visited.add(adjCell);
				//adjacencies can only be walkways or doors
				if(numSteps == 1 || adjCell.isDoorway() ) {
					targets.add(adjCell);
				}
				else {
					findAllTargets(adjCell, numSteps - 1);
				}
				visited.remove(adjCell);
			}
		}
	}

	//sets up call to recursive findAllTargets function
	public void calcTargets(int x, int y, int pathLength) {
		//set up data structures needed by findAllTargets
		targets = new HashSet<BoardCell>();
		visited = new HashSet<BoardCell>();
		BoardCell startCell = boardCellArray[x][y];
		visited.add(startCell);
		findAllTargets(startCell, pathLength);
	}

	//getter for targets list. MUST BE CALLED AFTER calcTargets, otherwise will be a null pointer
	public Set<BoardCell> getTargets() {
		return targets;
	}

	//contains test setup code so we can copy from tests if we want to have output inside a test
	public static void main(String[] args) {
		Board board ;
		board = Board.getInstance();
		board.setConfigFiles("data/testsMap.csv", "data/rooms.txt");
		board.initialize();
	}

}
