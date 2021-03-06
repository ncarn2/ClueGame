/*
 * Jordan Newport
 * Nicholas Carnival
 */
package clueGame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle.Control;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

// TODO: we use the same scanner method for every file--
// we should probably make a method called "loadFile()"
public class Board extends JPanel implements MouseListener{


	public static final int MAX_BOARD_SIZE = 50;
	public static final int CARDS_PER_PLAYER = 3;

	private String layoutFile;
	private String legendFile;
	private String playerConfigFile = "data/playerConfig.txt";

	private static final String COMMA = ",";

	private int numRows = 0;
	private int numColumns = 0;
	private int roll = 0;
	
	private Map<BoardCell, Set<BoardCell>> adjacencyMatrix;
	private Set<BoardCell> visited;
	private Set<BoardCell> targets = new HashSet<BoardCell>();

	private HashMap<Character, String> legendMap = new HashMap<Character, String>();
	
	private BoardCell[][] boardCellArray;

	private ArrayList<Card> weaponCardArray;
	private ArrayList<Card> roomCardArray;
	private ArrayList<Card> peopleCardArray;
	
	private ArrayList<Card> allCards; //cards remaining to be dealt/cards in the "deck"
	private ArrayList<Card> dealtCards; //cards that have been dealt

	//array of all the players
	private ArrayList<Player> allPlayers ;
	private ArrayList<BoardCell> clickedCells = new ArrayList<BoardCell>();
	private int currentPlayerIndex = 0;

	private HumanPlayer humanPlayer;

	private Player disprovingPlayer;
	private Solution guess;

	private Solution solution;
	
	private ControlPanel cp;
	
	public static final int  WIDTH  = 25;
	public static final int  HEIGHT = 25;

	// variable used for singleton pattern
	private static Board theInstance = new Board();

	// constructor is private to ensure only one can be created
	private Board() {}

	// this method returns the only Board
	public static Board getInstance() {
		return theInstance;
	}
	
	//sets the config file variables
	public void setConfigFiles(String layout, String legend ) {
		//test if the layout file is the correct length
		this.layoutFile = layout;
		this.legendFile = legend;
	}


	/*************************************************************
	 * Config Methods
	 *************************************************************/
	public void initialize() {

		try {
			setNumRows();
			setNumColumns();

			loadRoomConfig();
			loadBoardConfig();

			//loads the players from the 'playerConfig.txt' file
			loadPlayers();
			//loads the cards from the 'cards.csv' file
			loadCards();
			
			//sets the card solution
			setSolution();

			//deal the cards after loading the cards
			dealCards();

			//now that we have all our board cells, we can calculate each of their AdjacencyLists
			calcAdjacencies();
		} catch (BadConfigFormatException e) {
			System.out.println("Unable to initialize the board");
		} catch (FileNotFoundException e) {
			System.out.println("A specified file was not found");
		}

	}
	
	/*************************************************************
	 * Player Methods
	 *************************************************************/

	public int getCurrentPlayerIndex() {
		return currentPlayerIndex;
	}

	public void setCurrentPlayerIndex(int currentPlayerIndex) {
		this.currentPlayerIndex = currentPlayerIndex;
	}

	// sets up targets by rolling a die and calculating targets
	public int setUpMove() {
		roll = allPlayers.get(currentPlayerIndex).rollDie();
		calcTargets(allPlayers.get(currentPlayerIndex).location, roll);
		return roll;
	}	
	
	public int getRoll() {
		return roll;
	}

	// prints targets in a different color
	public void showTargets(Graphics g) {
		for (BoardCell b : targets) {
			b.draw(g, true);
		}
	}
	
	///////////////////////////////////////////THIS IS WHAT CAUSES THE ERRORS//////////////////////////////////////////

	// Does human move once a boardCell is selected
	public void doMove(BoardCell cell, Player player) {
		BoardCell oldLocation = player.getLocation();

		//no matter what, we add a player to the new location
		boardCellArray[cell.getRow()][cell.getColumn()].addPlayerCount();
		boardCellArray[cell.getRow()][cell.getColumn()].setPlayer(true);

		boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].subPlayerCount();

		//if this player was the only player at oldLocation 
		if(boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].getPlayerCount() == 0) {
			boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].setPlayer(false);
		}
		//set the player to have moved
		if (player instanceof HumanPlayer) {
			humanPlayer.setHasMoved(true);
		}

		//set the players location
		player.setLocation(boardCellArray[cell.getRow()][cell.getColumn()]);


		repaint();
	}

	// does computer move and updates board's view of player's location
	public void doMoveComputer() {
		BoardCell oldLocation = allPlayers.get(currentPlayerIndex).location;
		BoardCell newLocation = ((ComputerPlayer) allPlayers.get(currentPlayerIndex)).pickLocation(targets);

		boardCellArray[newLocation.getRow()][newLocation.getColumn()].addPlayerCount();
		boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].subPlayerCount();

		//if there are no players at the new location
		if(boardCellArray[newLocation.getRow()][newLocation.getColumn()].getPlayerCount() >= 0) {
			//set it to display a player
			boardCellArray[newLocation.getRow()][newLocation.getColumn()].setPlayer(true);
			//set the color
			boardCellArray[newLocation.getRow()][newLocation.getColumn()].setPlayerColor(allPlayers.get(currentPlayerIndex).getColor());
		}

		//if there were more than one player at the old location
		if(boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].getPlayerCount() > 1) {
			//keep the player color to true
			boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].setPlayer(true);
			//remove one
			boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].subPlayerCount();
		} 

		//if there are no players at the previous location
		if(boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].getPlayerCount() > 0){
			boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].setPlayer(true);
		} else {
			boardCellArray[oldLocation.getRow()][oldLocation.getColumn()].setPlayer(false);
		}
		repaint();
	}
	public HumanPlayer getHumanPlayer() {
		return humanPlayer;
	}

	//loads in all of the players from the config file
	public void loadPlayers() throws BadConfigFormatException, FileNotFoundException{
		Scanner scanner = new Scanner(new File(playerConfigFile));
		allPlayers = new ArrayList<Player>();
		
		scanner.useDelimiter(COMMA);
		Random random = new Random();

		//random number max size all players...
		currentPlayerIndex = random.nextInt(6);
		int count = 0;

        while (scanner.hasNextLine()) {
        	String currentLine = scanner.nextLine();
        	String[] splitLine = currentLine.split(COMMA);
        	
        	String playerName = splitLine[0].trim();
        	String playerColor = splitLine[1].trim();

        	//randomly create the human player
        	if(count == currentPlayerIndex) {
        		humanPlayer = new HumanPlayer(playerColor, playerName);
        		allPlayers.add(humanPlayer);
        	} else {
				//sets the computer players
				Player player = new ComputerPlayer(playerColor, playerName); 
				allPlayers.add(player);
        	}
        	count++;
        }

		scanner.close();
		
		//this method creates the possile starting positions
		setStartingLocations();
		
	}
	
	//sets the starting locations
	private void setStartingLocations() {
		Random random = new Random();

		BoardCell cell1 = boardCellArray[20][4];
		BoardCell cell2 = boardCellArray[14][10];
		BoardCell cell3 = boardCellArray[7][14];
		BoardCell cell4 = boardCellArray[5][4];
		BoardCell cell5 = boardCellArray[5][19];
		BoardCell cell6 = boardCellArray[12][17];
		BoardCell cell7 = boardCellArray[0][8];

		//there are seven start locations
		for(int i = 0; i < allPlayers.size(); i++) {
			int randomNumber = random.nextInt(7);
			//sets the position randomly
			switch (randomNumber) {
				case 0:
					if(!boardCellArray[20][4].isPlayer()) {
						allPlayers.get(i).setLocation(cell1);
						boardCellArray[20][4].setPlayer(true);
						boardCellArray[20][4].addPlayerCount();
						boardCellArray[20][4].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 1:
					if(!boardCellArray[14][10].isPlayer()) {
						allPlayers.get(i).setLocation(cell2);
						boardCellArray[14][10].setPlayer(true);
						boardCellArray[14][10].addPlayerCount();
						boardCellArray[14][10].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 2:
					if(!boardCellArray[7][14].isPlayer()) {
						allPlayers.get(i).setLocation(cell3);
						boardCellArray[7][14].setPlayer(true);
						boardCellArray[7][14].addPlayerCount();
						boardCellArray[7][14].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 3:
					if(!boardCellArray[5][4].isPlayer()) {
						allPlayers.get(i).setLocation(cell4);
						boardCellArray[5][4].setPlayer(true);
						boardCellArray[5][4].addPlayerCount();
						boardCellArray[5][4].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 4:
					if(!boardCellArray[5][19].isPlayer()) {
						allPlayers.get(i).setLocation(cell5);
						boardCellArray[5][19].setPlayer(true);
						boardCellArray[5][19].addPlayerCount();
						boardCellArray[5][19].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 5:
					if(!boardCellArray[12][17].isPlayer()) {
						allPlayers.get(i).setLocation(cell6);
						boardCellArray[12][17].setPlayer(true);
						boardCellArray[12][17].addPlayerCount();
						boardCellArray[12][17].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
				case 6:
					if(!boardCellArray[0][8].isPlayer()) {
						allPlayers.get(i).setLocation(cell7);
						boardCellArray[0][8].setPlayer(true);
						boardCellArray[0][8].addPlayerCount();
						boardCellArray[0][8].setPlayerColor(allPlayers.get(i).getColor());
					}
					else {
						i--;
					}
					break;
			}
			
		}
		
	}

	//checks if the accusation is equal to the solution
	public boolean validateAccusation(Solution accusation) {
		return (accusation.getWeapon().equals(getSolution().getWeapon())
				&& accusation.getRoom().equals(getSolution().getRoom())
				&& accusation.getPerson().equals(getSolution().getPerson())
				);
	}
	
	public void setPlayersList(ArrayList<Player> playerList) {
		this.allPlayers = playerList;
	}

	/*************************************************************
	 * Handle Suggestion Methods
	 *************************************************************/
	
	//this method returns the player that disproves the suggestion,
	public Player handleSuggestion(Solution accusation, Boolean isAccusation) {
		Player disprovenPlayer = null;
		Boolean disproven = false;

		//iterate over every player
		int count = (currentPlayerIndex + 1) % allPlayers.size();
		while (currentPlayerIndex != count) {
			Player p = allPlayers.get(count);
			//if it hasn't already been disproven
			if(!disproven) {
				//if the player has at least one of the cards in the accusation
				if(p.disproveSuggestion(accusation) == accusation.getPerson()
						|| p.disproveSuggestion(accusation) == accusation.getRoom()
						|| p.disproveSuggestion(accusation) == accusation.getWeapon()){
					disproven = true;
					disprovenPlayer = p;
					p.disprovingCard = p.disproveSuggestion(accusation);
					break;
				}
			}
			count = (count + 1) % allPlayers.size();
		}

		// figure out which player was suggested and move that player to the room
			for (Player p : allPlayers) {
				if (p.getName().equals(accusation.getPerson().getName())) {
					//only teleport on accusations
					if(!isAccusation) {
						doMove(allPlayers.get(currentPlayerIndex).location, p);
					} 
					break;
				}
			}

		return disprovenPlayer;
	}
	
	/*************************************************************
	 * Card Methods
	 *************************************************************/

	/*
	deals cards to all players
	MUST BE CALLED AFTER SETSOLUTION
	generates all the numbers from 0 to 17 randomly without repeats (with a set),
	deals the card at each one to the next player in line, and keeps track
	of cards that have been dealt and cards that remain to be dealt (none)
	
	Also makes sure that each player knows what board it's on
	*/
	public void dealCards() {
		for (Player p : allPlayers) {
			p.setBoard(this);
		}
		dealtCards = new ArrayList<Card>();
		Random r = new Random();
		for (int i = 0; i < allCards.size(); i++) {
			while (true) {
				int random = r.nextInt(allPlayers.size());
				if (allPlayers.get(random).getMyCards().size() >= CARDS_PER_PLAYER) {
					continue;
				} else {
					allPlayers.get(random).addCard(allCards.get(i));
					dealtCards.add(allCards.get(i));
				}
				break;
			}
		}
	}

	//This creates a new solution for each game
	public void setSolution() {
		
		Card solPerson;
		Card solWeapon;
		Card solRoom;
		
		//generate the random solution
		Random random = new Random();
		int randomPerson = random.nextInt((peopleCardArray.size()) ) ;
		int randomWeapon = random.nextInt((weaponCardArray.size()) ) ;
		int randomRoom = random.nextInt((roomCardArray.size()) ) ;

		solWeapon = weaponCardArray.get(randomWeapon);
		solPerson = peopleCardArray.get(randomPerson);
		solRoom = roomCardArray.get(randomRoom);

		solution = new Solution(solWeapon, solPerson, solRoom);

		// remove all cards that are in the solution from the list of all cards
		allCards.remove(solWeapon);
		allCards.remove(solPerson);
		allCards.remove(solRoom);
	}
	
	public Solution getSolution() {
		return solution;
	}

	public ArrayList<Card> getWeaponCards() {
		return weaponCardArray;
	}

	public ArrayList<Card> getPeopleCards() {
		return peopleCardArray;
	}

	public ArrayList<Card> getRoomCards() {
		return roomCardArray;
	}

	public void loadCards() throws BadConfigFormatException, FileNotFoundException{ 
		String cardConfigFile = "data/cards.csv";
		Scanner scanner = new Scanner(new File(cardConfigFile));

		String cardTypeString;
		CardType cardType;
		String cardName;
		
		//this will hold all of the cards and their types
		peopleCardArray = new ArrayList<Card>();
		weaponCardArray = new ArrayList<Card>();
		roomCardArray = new ArrayList<Card>();
		
		allCards = new ArrayList<Card>();
		
		scanner = new Scanner(new File(cardConfigFile));
		while(scanner.hasNextLine()) {

			String currentLine = scanner.nextLine();
			String[] cleanedLine = currentLine.split(COMMA);

			cardName = cleanedLine[0].trim();
			cardTypeString = cleanedLine[1].trim();

			switch (cardTypeString) {
				case "weapon":
					cardType = CardType.WEAPON;
					Card weaponCard = new Card(cardName, cardType); 
					weaponCardArray.add(weaponCard);
					allCards.add(weaponCard);
					break;
				case "room":
					cardType = CardType.ROOM;
					Card roomCard = new Card(cardName, cardType); 
					roomCardArray.add(roomCard);
					allCards.add(roomCard);
					break;
				case "person":
					cardType = CardType.PERSON;
					Card personCard = new Card(cardName, cardType); 
					peopleCardArray.add(personCard);
					allCards.add(personCard);
					break;
				default:
					break;
			}
		}
		scanner.close();

	}
	
	
	/*************************************************************
	 * Room Config Methods 
	 *************************************************************/
	public void loadRoomConfig() throws BadConfigFormatException, FileNotFoundException {
		//these two functions write to NumRows and NumColumns variables
		setNumRows();
		setNumColumns();
		//-1 is an error state for NumColumns
		if(numColumns == -1) {
			throw new BadConfigFormatException("Bad Columns");
		}
		boardCellArray = new BoardCell[numRows][numColumns];
		
		//Get scanner instance
		Scanner scanner = new Scanner(new File(legendFile));
         
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
        String legendCardType = "";

        //this is size three because of how the legend must be formatted
        String[] splitArray = new String[3];

        //opens the legend file
		scanner = new Scanner(new File(legendFile));
        //iterates through each line of the legend file and adds it to the legendMap
        for(int i = 0; i < count; i ++) {
        	valueArray[i] = scanner.nextLine();
        	splitArray = valueArray[i].split(COMMA);

        	legendLetter = splitArray[0].charAt(0);
        	legendRoom = splitArray[1];
        	legendCardType = splitArray[2];
        	
        	legendRoom = legendRoom.trim();
        	legendCardType = legendCardType.trim();
        	
        	//if the legend has something that is neither a regular room nor a walkway/closet
        	if(!legendCardType.contentEquals("Card") && !legendCardType.contentEquals("Other")) {
        		scanner.close();
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
		setNumColumns();
		setNumRows();

		if(numColumns > MAX_BOARD_SIZE || numRows > MAX_BOARD_SIZE) {
			throw new BadConfigFormatException("Board size exceeds max board size of "
				+ MAX_BOARD_SIZE + " in at least one dimension");
		}

		//numColumns will be -1 if the columns are formatted improperly
		if(numColumns == -1) {
			throw new BadConfigFormatException("Bad column format");

		//loads the csv if the columns are proper
		} else {
			try {
				scanner = new Scanner(new File(layoutFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			//gridLine should store every line in the csv file
			String[] gridLine = new String[numRows];

			//stores a line of the file without commas
			String[] cleanedGridLine = new String[numColumns];

			//goes through every column in the csv file and scans the entire line
			for(int row = 0; row < numRows; row++) {
				gridLine[row] = scanner.nextLine();
				cleanedGridLine = gridLine[row].split(COMMA);

				//scans each string and removes the commas
				for(int column = 0; column < numColumns; column++) {
					//create a new board cell at a certain location with its char
					boardCellArray[row][column] = new BoardCell(row,column);
					boardCellArray[row][column].setBoard(this);

					//checks that the initial string is not a door
					//i.e. if there is not more than one char or the second char is N
					if(cleanedGridLine[column].length() == 1 || cleanedGridLine[column].charAt(1) == 'N'){
						//handle cells whose second character is N
						if(cleanedGridLine[column].length() > 1) {
							if(cleanedGridLine[column].charAt(1) == 'N') {
								boardCellArray[row][column].setNameDrawer(true);
							}
						}
						//load in initial character for board cell
						boardCellArray[row][column].setInitial(cleanedGridLine[column].charAt(0));

						//sets walkways
						if(cleanedGridLine[column].charAt(0) == 'W') {
							boardCellArray[row][column].setWalkway(true);
						}

						//checks that the character is actually in the map
						if(!legendMap.containsKey(boardCellArray[row][column].getInitial())) {
							throw new BadConfigFormatException("Error: This Character is not in the Legend: "
									+ boardCellArray[row][column].getInitial());
						}
						//everything other than a walkway or closet is a room
						if(!cleanedGridLine[column].equals("X") && !cleanedGridLine[column].contentEquals("W")) {
							//tells the cell that it is a room
							boardCellArray[row][column].setRoom(true);
							//sets the name of the room that that cell is in
							boardCellArray[row][column].setRoomName(legendMap.get(boardCellArray[row][column].getInitial()));
						} 
						if(cleanedGridLine[column].equals("X") ) {
							boardCellArray[row][column].setIsCloset(true);
						}
						
						
					//this runs if the initial string is longer than 1 char 
					} else {
						//load in initial character for board cell
						boardCellArray[row][column].setInitial(cleanedGridLine[column].charAt(0));

						//set up which direction the door is facing
						char doorDirectionLetter = cleanedGridLine[column].charAt(1);
						boardCellArray[row][column].setDoorway(true);
						boardCellArray[row][column].setRoom(true);
						boardCellArray[row][column].setRoomName(legendMap.get(boardCellArray[row][column].getInitial()));

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
		}
	}

	//this contains the legend e.g. 'K, Kitchen'
	public Map<Character, String> getLegend() {
		return legendMap;
	}


	/*************************************************************
	 * Set Board Dimension 
	 *************************************************************/

	//gets the number of columns in the specified csv file
	private int setNumColumns() {

		Scanner scanner = null;
		List<String> csvList = Arrays.asList();
		try {
			scanner = new Scanner(new File(layoutFile));

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
					numColumns = -1;
					return numColumns;
				}
			}
		} catch (FileNotFoundException e) {
			//not really anything better to do with this
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		numColumns = csvList.size();
		return numColumns;
	}
	
	public int getNumColumns() {
		return numColumns;
	}

	//counts the number of lines in the text file
	private int setNumRows() {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(layoutFile));
			numRows = 0;
			while(scanner.hasNextLine()) {
				numRows++;
				scanner.nextLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		return numRows;
	}

	public int getNumRows() {
		return numRows;
	}
	
	/*************************************************************
	 * Board Adjacency  
	 *************************************************************/

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
		for (int row = 0; row < numRows; row++) {
			//for every column
			for (int column = 0; column < numColumns; column++) {
				//put the current cell into the key of the hash map
				adjacencyMatrix.put(boardCellArray[row][column], new HashSet<BoardCell>());
				//if this cell is a walkway we will check its adjacencies this way:
				//all adjacent walkways, or any doors that are facing the correct direction
				//it does check if a cell is in bounds first--to avoid null pointer
				if (boardCellArray[row][column].getInitial() == 'W') {
					//if the board cell is a walkway, setWalkway to true
					if (row > 0) {
						if (boardCellArray[row-1][column].isWalkway() ||
								boardCellArray[row-1][column].getDoorDirection() == DoorDirection.DOWN) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row-1][column]);
						}
					}
					if (column > 0) {
						if (boardCellArray[row][column-1].isWalkway() ||
								boardCellArray[row][column-1].getDoorDirection() == DoorDirection.RIGHT) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column-1]);
						}
					}
					if (row < numRows - 1) {
						if (boardCellArray[row+1][column].isWalkway() ||
								boardCellArray[row+1][column].getDoorDirection() == DoorDirection.UP) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row+1][column]);
						}
					}
					if (column < numColumns - 1) {
						if (boardCellArray[row][column+1].isWalkway() ||
								boardCellArray[row][column+1].getDoorDirection() == DoorDirection.LEFT) {
							adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column+1]);
						}
					}
				//otherwise if this cell is a doorway, then the only adjacency will be the walkway right outside
				} else if (boardCellArray[row][column].isDoorway()) {
					switch (boardCellArray[row][column].getDoorDirection()) {
					case LEFT:
						if (column > 0) {
							if(boardCellArray[row][column-1].isWalkway()) {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column-1]);
							}
						}
						break;
					case UP:
						if (row > 0) {
							if(boardCellArray[row-1][column].isWalkway()) {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row-1][column]);
							}
						}
						break;
					case RIGHT:
						if (column < numColumns - 1) {
							if(boardCellArray[row][column+1].isWalkway()) {
								adjacencyMatrix.get(boardCellArray[row][column]).add(boardCellArray[row][column+1]);
							}
						}
						break;
					case DOWN:
						if (row < numRows - 1) {
							if(boardCellArray[row+1][column].isWalkway()) {
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
		BoardCell startCell = boardCellArray[x][y];
		calcTargets(startCell, pathLength);
	}
	
	public void calcTargets(BoardCell b, int pathLength) {
		targets = new HashSet<BoardCell>();
		visited = new HashSet<BoardCell>();
		visited.add(b);
		findAllTargets(b, pathLength);
	}


	public BoardCell[][] getBoardCellArray() {
		return boardCellArray;
	}
	// getter for targets list.
	// MUST BE CALLED AFTER calcTargets, otherwise will be a null pointer
	public Set<BoardCell> getTargets() {
		return targets;
	}
	
	public ArrayList<Player> getPlayers() {
		return allPlayers;
	}

	public ArrayList<Card> getDealtCards() {
		return dealtCards;
	}
	
	/*************************************************************
	 * GUI Methods 
	 *************************************************************/

	@Override
	public void paintComponent(Graphics g) {
		ArrayList<BoardCell> nameDrawers = new ArrayList<BoardCell>();
		for (int column = 0; column < numColumns; column++) {
			for (int row = 0; row < numRows; row++) {
				BoardCell b = boardCellArray[row][column].draw(g, false); 
				if(b != null) {
					nameDrawers.add(b);
				}
			}
		}
		for(BoardCell b : nameDrawers) {
			b.draw(g, false);
		}
		
		if (currentPlayerIndex >= 0 && allPlayers.get(currentPlayerIndex) == humanPlayer && !humanPlayer.hasMoved() && humanPlayer.isTurn()) {
			showTargets(g);
		}

		boardCellArray[0][0].draw(g, false);
		
	}

	//this happens everytime the mouse is clicked
	@Override
	public void mouseClicked(MouseEvent event) {
		if(humanPlayer.isTurn()) {
			BoardCell clickedCell = null;
			//clear the clickedCells array
			clickedCells = new ArrayList<BoardCell>();
			Point p = event.getPoint();
			int row = 0, column = 0;

			row = (p.y/25) - 2;
			column = (p.x/25);

			//check if the cell is in bounds
			if(row <= numRows && column <= numColumns) {
				clickedCell = boardCellArray[row][column];
			}
			if(targets.contains(clickedCell)) {
				clickedCells.add(clickedCell);
				drawPlayerPosition(clickedCell);
			} else {
				//display a pop up window if the wrong cell was clicked
				JOptionPane.showMessageDialog(this,"You cannot move there");
			}
		}
	}

	//this method moves the player to his new position and clears the old cell
	private void drawPlayerPosition(BoardCell clickedCell) {

		//if the clickedCells array has cells, and the player has not moved
		if(clickedCells.size() > 0 &&  !humanPlayer.hasMoved()) {
			//if the current cell is in targets
			if(clickedCells.contains(clickedCell)) {
				//removes the player icon from his original cell
				BoardCell originalCell = humanPlayer.getLocation();
				boardCellArray[originalCell.getRow()][originalCell.getColumn()].subPlayerCount();
				BoardCell temp = boardCellArray[originalCell.getRow()][originalCell.getColumn()];

				if(boardCellArray[originalCell.getRow()][originalCell.getColumn()].getPlayerCount() == 0) {
					boardCellArray[originalCell.getRow()][originalCell.getColumn()].setPlayer(false);
				}

				//moves the player icon to the new cell
				boardCellArray[clickedCell.getRow()][clickedCell.getColumn()].setPlayer(true);
				boardCellArray[clickedCell.getRow()][clickedCell.getColumn()].addPlayerCount();
				boardCellArray[clickedCell.getRow()][clickedCell.getColumn()].setPlayerColor(humanPlayer.getColor());
				humanPlayer.setLocation(clickedCell);

				//if the human is in a room, make an suggestion 
				if(humanPlayer.getLocation().isRoom()) {
					MakeAGuess mg = new MakeAGuess(this, false, cp);
					mg.setVisible(true);
				}
				//make sure that it is not the human players turn
				humanPlayer.setHasMoved(true);
				humanPlayer.setIsTurn(false);
				//empty the targets array
				targets = new HashSet<BoardCell>();
				//repaint the board with the new location and without the targets displayed
				repaint();
			} 
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent event) {}
	@Override
	public void mouseExited(MouseEvent event) {}
	@Override
	public void mousePressed(MouseEvent event) {}
	@Override
	public void mouseReleased(MouseEvent event) {}

	public Player getDisprovingPlayer() {
		return disprovingPlayer;
	}

	public void setDisprovingPlayer(Player disprovingPlayer) {
		this.disprovingPlayer = disprovingPlayer;
	}

	public Solution getGuess() {
		return guess;
	}

	public void setGuess(Solution guess) {
		this.guess = guess;
	}

	public void setCP(ControlPanel cp) {
		this.cp = cp;
	}

}
