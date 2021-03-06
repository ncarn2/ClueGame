package tests;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import clueGame.Board;
import clueGame.BoardCell;
import clueGame.Card;
import clueGame.CardType;
import clueGame.ComputerPlayer;
import clueGame.HumanPlayer;
import clueGame.Player;
import clueGame.Solution;

public class gameActionTests {
	
	/*
	 	What class is supposed to do what:

		Selecting a target location - ComputerPlayer
		Checking an accusation - Board
		Disproving a suggestion - Player
		Handling a suggestion - Board
		Creating a suggestion - ComputerPlayer

	 */
	private static Board board;
	private ComputerPlayer npc;
	
	@Before
	public void setUp() {
		// Board is singleton, get the only instance
		board = Board.getInstance();
		// set the file names to use my config files
		board.setConfigFiles("data/testsMap.csv", "data/rooms.txt");		
		board.initialize();
		npc = new ComputerPlayer("red", "Jimothy Jenkins");
		npc.setBoard(board);
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

		npc.pickLocation(targets);
		assertEquals(npc.getLocation(), cell3);
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

		BoardCell temp;
		//Have the Computer pick a location 10000 times and then check that it picked each cell at least once
		for(int i = 0; i < 10000; i++) {
			npc.pickLocation(targets);
			temp = npc.getLocation();
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

		board.calcTargets(5, 2, 2);

		Set<BoardCell> targets = board.getTargets(); 
		Set<BoardCell> randomCells = new HashSet<BoardCell>();
		
		targets.add(cell1);
		targets.add(cell2);
		targets.add(cell3);
		
		npc.setLastVisited(cell3);

		BoardCell temp;
		//Have the Computer pick a location 10000 times and then check that it picked each cell at least once
		for(int i = 0; i < 10000; i++) {
			npc.pickLocation(targets);
			temp = npc.getLocation();
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
		Solution accusation = new Solution(weaponCard, peopleCard, roomCard);
		
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
		Solution accusation = new Solution(weaponCard, peopleCard, roomCard);
		
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
		Solution accusation = new Solution(weaponCard, peopleCard, roomCard);
		
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
		Solution accusation = new Solution(weaponCard, peopleCard, roomCard);
		
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
    */
	// test that if a player has only one card it is used to disprove a suggestion
	@Test
	public void testDisproveSuggestionOneMatchingCard() {
		// give a computer player one card, plus some irrelevant cards to fuzz
		Card card = new Card("first", CardType.PERSON);
		npc.addCard(card);
		npc.addCard(new Card("second", CardType.PERSON));
		npc.addCard(new Card("third", CardType.PERSON));
		npc.addCard(new Card("fourth", CardType.PERSON));
		npc.addCard(new Card("fifth", CardType.PERSON));
		npc.addCard(new Card("sixth", CardType.PERSON));
		// 
		Solution suggestion = new Solution(new Card("weapon", CardType.WEAPON),
				card, new Card("room", CardType.ROOM));
		Card match = npc.disproveSuggestion(suggestion);
		assertEquals(card, match);
	}
	
	// test that if a player has multiple matching cards one should be returned randomly
	@Test
	public void testDisproveSuggestionMultipleMatchingCards() {
		// give a computer player one of each card, plus some irrelevant cards to fuzz
		Card card = new Card("first", CardType.PERSON);
		npc.addCard(card);
		npc.addCard(new Card("second", CardType.PERSON));
		Card card2 = new Card("third", CardType.WEAPON);
		npc.addCard(card2);
		npc.addCard(new Card("fourth", CardType.WEAPON));
		Card card3 = new Card("fifth", CardType.ROOM);
		npc.addCard(card3);
		npc.addCard(new Card("sixth", CardType.ROOM));
		// pass in the solution with the same one of each card that the player has
		Solution suggestion = new Solution(card2, card, card3);
		
		// matches will hold the cards returned.
		// Should eventually get card1, card2, card3 and have size 3
		Set<Card> matches = new HashSet<Card>();
		for (int i = 0; i < 1000; i++) {
			matches.add(npc.disproveSuggestion(suggestion));
		}
		assertEquals(3, matches.size());
	}
	
	// test that if a player has no matching cards then disproving the suggestion returns null
	@Test
	public void testDisproveSuggestionNoMatches() {
		Solution suggestion = new Solution(new Card("weapon", CardType.WEAPON),
				new Card("person", CardType.PERSON), new Card("room", CardType.ROOM));
		Card card = npc.disproveSuggestion(suggestion);
		assertNull(card);
	}

	
	/*
	(15pts) Handle suggestion - Board. Tests include:
	 */
	
	//Suggestion no one can disprove returns null
	@Test
	public void testNoDisprove() {
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");

		Solution solution = board.getSolution();

		Solution accusation = new Solution(
				new Card("String", CardType.WEAPON),
				new Card("Garfiel", CardType.PERSON),
				new Card("Kitchen", CardType.ROOM));
		
		//give all computer players some player cards
		npc1.addCard(new Card("Daveed", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("Jack", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));

		Card disproveCard1 = npc1.disproveSuggestion(accusation);
		Card disproveCard2 = npc2.disproveSuggestion(accusation);
		Card disproveCard3 = npc3.disproveSuggestion(accusation);
		
		assertEquals(null, disproveCard1);
		assertEquals(null, disproveCard2);
		assertEquals(null, disproveCard3);
	}
	
	//Suggestion only accusing player can disprove returns null
	@Test
	public void  testAccusingDisprove() {
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");

		Solution solution = board.getSolution();
		
		//give all computer players some player cards
		npc1.addCard(new Card("Daveed", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("Jack", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));
		

		//places the player in the kitchen
		BoardCell cell = board.getCellAt(2, 4);


		npc1.setLocation(cell);
		npc1.setBoard(board);

		Solution accusation = npc1.createSuggestion();

		Card disproveCard1 = npc1.disproveSuggestion(accusation);
		Card disproveCard2 = npc2.disproveSuggestion(accusation);
		Card disproveCard3 = npc3.disproveSuggestion(accusation);
		
		assertEquals(null, disproveCard1);
		assertEquals(null, disproveCard2);
		assertEquals(null, disproveCard3);
	}
	
	@Test
	//Suggestion only human can disprove returns answer (i.e., card that disproves suggestion)
	public void testHumanDisprove() {
		
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");

		HumanPlayer player = new HumanPlayer("black", "You");

		Card accusationWeapon = new Card("String", CardType.WEAPON);
		Card accusationPerson = new Card("Garfiel", CardType.PERSON);
		Card accusationRoom = new Card("Kitchen", CardType.ROOM);
		
		Solution  accusation = new Solution(accusationRoom, accusationPerson, accusationWeapon);
		
		//give all computer players some player cards
		npc1.addCard(new Card("Daveed", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		player.addCard(accusationPerson);
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("Jack", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));
		player.addCard(new Card("Pencil", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));
		player.addCard(new Card("Pueblo", CardType.ROOM));
		
		Card playerDisprove = player.disproveSuggestion(accusation);
		
		//this tests that the player showed the right card to disprove
		assertEquals(accusationPerson, playerDisprove);
		
		
	}
	
	//Suggestion only human can disprove, but human is accuser, returns null
	@Test
	public void testHumanAccuser() {
		
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");
		
		HumanPlayer player = new HumanPlayer("black", "You");
		
		//give all computer players some player cards
		npc1.addCard(new Card("Daveed", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		player.addCard(new Card("Garfiel", CardType.PERSON));
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("Jack", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));
		player.addCard(new Card("String", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));
		player.addCard(new Card("Portugal", CardType.ROOM));
		

		//places the player in the kitchen
		BoardCell cell = board.getCellAt(2, 4);


		player.setLocation(cell);
		player.setBoard(board);

		Solution accusation = player.createSuggestion();

		Card disproveCard1 = npc1.disproveSuggestion(accusation);
		Card disproveCard2 = npc2.disproveSuggestion(accusation);
		Card disproveCard3 = npc3.disproveSuggestion(accusation);
		Card humanDisprove = player.disproveSuggestion(accusation);
		
		assertEquals(null, disproveCard1);
		assertEquals(null, disproveCard2);
		assertEquals(null, disproveCard3);
		assertEquals(null, humanDisprove);
	}
	
	@Test
	//Suggestion that two players can disprove, correct player (based on starting with next player in list) returns answer
	public void testTwoPlayerDisprove() {
		
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");
		
		ArrayList<Player> playerList = new ArrayList<Player>();

		playerList.add(npc1);
		playerList.add(npc2);
		playerList.add(npc3);

		board.setPlayersList(playerList);

		Solution accusation = new Solution(
				new Card("String", CardType.WEAPON),
				new Card("Garfiel", CardType.PERSON),
				new Card("Kitchen", CardType.ROOM));

		//give all computer players some player cards
		npc1.addCard(new Card("Garfiel", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("String", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));
		
		//we need to create a list of players and determine which order they disprove
		ComputerPlayer disprovingPlayer = null;

		board.setCurrentPlayerIndex(0);
		disprovingPlayer = (ComputerPlayer) board.handleSuggestion(accusation, true);

		assertEquals(npc2, disprovingPlayer);
	}
	
	@Test
    //Suggestion that human and another player can disprove, other player is next in list, ensure other player returns answer
	public void testHumanAndNPCDisprove() {
		
		ComputerPlayer npc1 = new ComputerPlayer("purple", "Abraham Lincoln");
		ComputerPlayer npc2 = new ComputerPlayer("yellow", "Babraham Bincoln" );
		ComputerPlayer npc3 = new ComputerPlayer("red", "David Johnson");
		
		HumanPlayer player = new HumanPlayer("black", "You");
		
		ArrayList<Player> playerList = new ArrayList<Player>();

		playerList.add(player);
		playerList.add(npc1);
		playerList.add(npc2);
		playerList.add(npc3);

		board.setPlayersList(playerList);

		Solution accusation = new Solution(
				new Card("String", CardType.WEAPON),
				new Card("Garfiel", CardType.PERSON),
				new Card("Kitchen", CardType.ROOM));

		//give all computer players some player cards
		npc1.addCard(new Card("Garfiel", CardType.PERSON));
		npc2.addCard(new Card("Jason", CardType.PERSON));
		npc3.addCard(new Card("Alex", CardType.PERSON));
		player.addCard(new Card("Garfiel", CardType.PERSON));
		
		//give all computer players some weapon cards
		npc1.addCard(new Card("Dagger", CardType.WEAPON));
		npc2.addCard(new Card("String", CardType.WEAPON));
		npc3.addCard(new Card("Axe", CardType.WEAPON));
		player.addCard(new Card("Pencil", CardType.WEAPON));

		//give all computer players some room cards
		npc1.addCard(new Card("Denver", CardType.ROOM));
		npc2.addCard(new Card("Jacksonville", CardType.ROOM));
		npc3.addCard(new Card("Alabame", CardType.ROOM));
		player.addCard(new Card("Paraguay", CardType.ROOM));
		
		board.handleSuggestion(accusation, true);
		board.setPlayersList(playerList);
		
		board.setCurrentPlayerIndex(0);
		Player disprovenPlayer = board.handleSuggestion(accusation, true);
		
		assertEquals(npc1, disprovenPlayer);
	}

}