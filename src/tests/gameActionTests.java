package tests;
/*
    Selecting a target location - ComputerPlayer
    Checking an accusation - Board
    Disproving a suggestion - Player
    Handling a suggestion - Board
    Creating a suggestion - ComputerPlayer

 */
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import clueGame.Accusation;
import clueGame.Board;
import clueGame.BoardCell;
import clueGame.Card;
import clueGame.ComputerPlayer;
import clueGame.Player;
import clueGame.Solution;

public class gameActionTests {
	
	private static Board board;
	
	@BeforeClass
	public static void setUp() {
		// Board is singleton, get the only instance
		board = Board.getInstance();
		// set the file names to use my config files
		board.setConfigFiles("data/testsMap.csv", "data/rooms.txt");		
		board.initialize();
	}
	//this tests that the Computer Player chooses to enter a room given other options
	@Test
	public void testPickRoom() {
		//cell1 and cell2 are walkways
		BoardCell cell1 = board.getCellAt(0, 0);
		BoardCell cell2 = board.getCellAt(20, 18);
		//cell3 is a door
		BoardCell cell3 = board.getCellAt(2, 4);

		Set<BoardCell> targets = new HashSet<BoardCell>();  

		targets.add(cell1);
		targets.add(cell2);
		targets.add(cell3);

		ComputerPlayer NPC = new ComputerPlayer("red", "Jimothy Jenkins");
		NPC.pickLocation(targets);
		assertEquals(NPC.getLocation(), cell3);
	}
	//tests that the computer player chooses a different walkway each time
	@Test
	public void testPickRandom() {
		BoardCell cell1 = board.getCellAt(5, 2);
		BoardCell cell2 = board.getCellAt(8, 4);
		BoardCell cell3 = board.getCellAt(7, 4);

		Set<BoardCell> targets = new HashSet<BoardCell>();  
		Set<BoardCell> randomCells = new HashSet<BoardCell>();

		targets.add(cell1);
		targets.add(cell2);
		targets.add(cell3);

		ComputerPlayer NPC = new ComputerPlayer("red", "Jimothy Jenkins");
		
		BoardCell temp;
		//Have the Computer pick a location 10000 times and then check that it picked each cell at least once
		for(int i = 0; i < 10000; i++) {
			NPC.pickLocation(targets);
			temp = NPC.getLocation();
			randomCells.add(temp);
		}

		//This checks that all three cells were chosen
		assertEquals(true,((randomCells.contains(cell1) && randomCells.contains(cell2) && randomCells.contains(cell3))));

	}
	
	@Test
	public void testLastVisited() {
		BoardCell cell1 = board.getCellAt(5, 2);
		BoardCell cell2 = board.getCellAt(8, 4);
		//these are both doors
		BoardCell cell3 = board.getCellAt(7, 4);
		BoardCell cell4 = board.getCellAt(7, 4);

		ComputerPlayer NPC = new ComputerPlayer("red", "Jimothy Jenkins");
		
		board.calcTargets(5, 2, 2);

		Set<BoardCell> targets = board.getTargets(); 
		Set<BoardCell> randomCells = new HashSet<BoardCell>();
		
		System.out.println(cell1);
		System.out.println(cell2);
		System.out.println(cell3);
		System.out.println(cell4);

		targets.add(cell1);
		targets.add(cell2);
		targets.add(cell3);
		
		NPC.setLastVisited(cell3);

		BoardCell temp;
		//Have the Computer pick a location 10000 times and then check that it picked each cell at least once
		for(int i = 0; i < 10000; i++) {
			NPC.pickLocation(targets);
			temp = NPC.getLocation();
			randomCells.add(temp);
		}
		//This checks that all three cells were chosen
		assertEquals(true,((randomCells.contains(cell1) && randomCells.contains(cell2) && randomCells.contains(cell3))));
	}

	/*
	 * (15pts) Make an accusation. Tests include:

    solution that is correct
    solution with wrong person
    solution with wrong weapon
    solution with wrong room
	*/
	// figure out the correct solution by process of elimination 
	// then pass it in as an accusation
	@Test
	public void testCorrectAccusation() {
		Solution testSolution = board.getSolution();
		ArrayList<Card> dealtCards = board.getDealtCards();
		ArrayList<Card> weaponCards = board.getWeaponCards();
		ArrayList<Card> roomCards = board.getRoomCards();
		ArrayList<Card> peopleCards = board.getPeopleCards();
		Card roomCard = null, peopleCard = null, weaponCard = null;
		for(Card c : weaponCards) {
			if(!dealtCards.contains(c)) {
				weaponCard = c;
			}
		}
		for(Card c : roomCards) {
			if(!dealtCards.contains(c)) {
				roomCard = c;
			}
		}
		for(Card c : peopleCards) {
			if(!dealtCards.contains(c)) {
				peopleCard = c;
			}
		}
		Accusation accusation = new Accusation(weaponCard, peopleCard, roomCard);
		
		assertEquals(true, board.validateAccusation(accusation));

	}

	// pass in the correct room and weapon, but the first person we find
	// that is not correct
	@Test
	public void testWrongPerson() {
		ArrayList<Card> dealtCards = board.getDealtCards();
		ArrayList<Card> weaponCards = board.getWeaponCards();
		ArrayList<Card> roomCards = board.getRoomCards();
		ArrayList<Card> peopleCards = board.getPeopleCards();
		Card roomCard = null, peopleCard = null, weaponCard = null;
		for(Card c : weaponCards) {
			if(!dealtCards.contains(c)) {
				weaponCard = c;
			}
		}
		for(Card c : roomCards) {
			if(!dealtCards.contains(c)) {
				roomCard = c;
			}
		}
		for(Card c : peopleCards) {
			if(dealtCards.contains(c)) {
				peopleCard = c;
			}
		}
		Accusation accusation = new Accusation(weaponCard, peopleCard, roomCard);
		
		assertNotEquals(true, board.validateAccusation(accusation));

	}

	// pass in the correct room and person, but the first weapon we find
	// that is not correct
	@Test
	public void testWrongWeapon() {
		ArrayList<Card> dealtCards = board.getDealtCards();
		ArrayList<Card> weaponCards = board.getWeaponCards();
		ArrayList<Card> roomCards = board.getRoomCards();
		ArrayList<Card> peopleCards = board.getPeopleCards();
		Card roomCard = null, peopleCard = null, weaponCard = null;
		for(Card c : weaponCards) {
			if(dealtCards.contains(c)) {
				weaponCard = c;
			}
		}
		for(Card c : roomCards) {
			if(!dealtCards.contains(c)) {
				roomCard = c;
			}
		}
		for(Card c : peopleCards) {
			if(!dealtCards.contains(c)) {
				peopleCard = c;
			}
		}
		Accusation accusation = new Accusation(weaponCard, peopleCard, roomCard);
		
		assertNotEquals(true, board.validateAccusation(accusation));

	}

	// pass in the correct person and weapon, but the first room we find
	// that is not correct

	@Test
	public void testWrongRoom() {
		ArrayList<Card> dealtCards = board.getDealtCards();
		ArrayList<Card> weaponCards = board.getWeaponCards();
		ArrayList<Card> roomCards = board.getRoomCards();
		ArrayList<Card> peopleCards = board.getPeopleCards();
		Card roomCard = null, peopleCard = null, weaponCard = null;
		for(Card c : weaponCards) {
			if(!dealtCards.contains(c)) {
				weaponCard = c;
			}
		}
		for(Card c : roomCards) {
			if(dealtCards.contains(c)) {
				roomCard = c;
			}
		}
		for(Card c : peopleCards) {
			if(!dealtCards.contains(c)) {
				peopleCard = c;
			}
		}
		Accusation accusation = new Accusation(weaponCard, peopleCard, roomCard);
		
		assertNotEquals(true, board.validateAccusation(accusation));

	}
	/*
	(15pts) Create suggestion. Tests include:

    Room matches current location
    If only one weapon not seen, it's selected
    If only one person not seen, it's selected (can be same test as weapon)
    If multiple weapons not seen, one of them is randomly selected
    If multiple persons not seen, one of them is randomly selected
    */
	// make sure that a suggestion that is made in a room involves that room
	@Test
	public void testRoomMatchesCurrentLocation() {
		ComputerPlayer npc = new ComputerPlayer("red", "Jimothy Jenkins");
		npc.setBoard(board);
		// put the player in the library
		BoardCell cell = board.getCellAt(5, 10);
		npc.setLocation(cell);
		// get the library from the board
		Card roomCard = null;
		for (Card c : board.getRoomCards()) {
			if (c.getName().equals("Library")) {
				roomCard = c;
				break;
			}
		}
		// make sure the suggestion they make has them in the library
		Solution suggestion = npc.createSuggestion();
		assertEquals(roomCard, suggestion.getRoom());
		
		// now put the player in the kitchen
		cell = board.getCellAt(2, 4);
		npc.setLocation(cell);
		// get the kitchen from the board
		for (Card c : board.getRoomCards()) {
			if (c.getName().equals("Kitchen")) {
				roomCard = c;
				break;
			}
		}
		// make sure the suggestion they make has them in the kitchen
		suggestion = npc.createSuggestion();
		assertEquals(roomCard, suggestion.getRoom());

		// now put the player in the bedroom
		cell = board.getCellAt(15, 7);
		npc.setLocation(cell);
		// get the bedroom from the board
		for (Card c : board.getRoomCards()) {
			if (c.getName().equals("Bedroom")) {
				roomCard = c;
				break;
			}
		}
		// make sure the suggestion they make has them in the bedroom
		suggestion = npc.createSuggestion();
		assertEquals(roomCard, suggestion.getRoom());
	}

	//make sure that if only one person or weapon is not seen it is suggested
	@Test
	public void testOneUnseenCardSuggestion() {
		ComputerPlayer npc = new ComputerPlayer("red", "Jimothy Jenkins");
		// needs to be in a location, but it doesn't really matter which one
		BoardCell cell = board.getCellAt(0, 0);
		npc.setLocation(cell);
		// computer will see all weapon and people cards except for the last of each
		for(int i = 0; i < board.getWeaponCards().size() - 1; i++) {
			if(!npc.getSeenCards().contains(board.getWeaponCards().get(i))) {
				npc.seeCard(board.getWeaponCards().get(i));
			}
		}
		for(int i = 0; i < board.getPeopleCards().size() - 1; i++) {
			if(!npc.getSeenCards().contains(board.getPeopleCards().get(i))) {
				npc.seeCard(board.getPeopleCards().get(i));
			}
		}
		npc.setBoard(board);
		Card lastWeaponCard = board.getWeaponCards().get(board.getWeaponCards().size() - 1);
		Card lastPeopleCard = board.getPeopleCards().get(board.getPeopleCards().size() - 1);
		Solution suggestion = npc.createSuggestion();
		// make sure that the card the computer suggests is the one that hasn't been seen
		assertEquals(lastPeopleCard, suggestion.getPerson());
		assertEquals(lastWeaponCard, suggestion.getWeapon());
	}
	
	// test that if a player has not seen multiple cards then it will randomly suggest any of them
	@Test
	public void testMultipleUnseenCards() {
		ComputerPlayer npc = new ComputerPlayer("red", "Jimothy Jenkins");
		npc.setBoard(board);
		BoardCell cell = board.getCellAt(0, 0);
		npc.setLocation(cell);
		HashSet<Card> people = new HashSet<Card>();
		HashSet<Card> weapons = new HashSet<Card>();
		for (int i = 0; i < 1000; i++) {
			Solution suggestion = npc.createSuggestion();
			people.add(suggestion.getPerson());
			weapons.add(suggestion.getWeapon());
		}
		assertEquals(6, people.size());
		assertEquals(6, weapons.size());
	}

	/*

	(15pts) Disprove suggestion - ComputerPlayer. Tests include:

    If player has only one matching card it should be returned
    If players has >1 matching card, returned card should be chosen randomly
    If player has no matching cards, null is returned

	(15pts) Handle suggestion - Board. Tests include:

    Suggestion no one can disprove returns null
    Suggestion only accusing player can disprove returns null
    Suggestion only human can disprove returns answer (i.e., card that disproves suggestion)
    Suggestion only human can disprove, but human is accuser, returns null
    Suggestion that two players can disprove, correct player (based on starting with next player in list) returns answer
    Suggestion that human and another player can disprove, other player is next in list, ensure other player returns answer

	 */
}





