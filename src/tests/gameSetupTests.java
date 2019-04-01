package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.util.ArrayList;

/*
 * This JUnit test, tests the the people are loaded properly,
 * that the deck of cards is both loaded and created properly,
 * and that dealing the cards works
 *
*/


import org.junit.BeforeClass;
import org.junit.Test;

import clueGame.Board;
import clueGame.Card;
import clueGame.CardType;
import clueGame.HumanPlayer;
import clueGame.Solution;

public class gameSetupTests {
	
	private static Board board;

	private HumanPlayer kernel = new HumanPlayer("yellow", "Kernel Mustard");
	// This runs before every test 
	
	@BeforeClass
	public static void setUp() {
		// Board is singleton, get the only instance
		board = Board.getInstance();
		// set the file names to use my config files
		board.setConfigFiles("data/CTest_ClueLayout.csv", "data/CTest_ClueLegend.txt");		
		board.initialize();
		
	}
	
	/*************************************************************
	 *  Testing the Player's Existence
	 *************************************************************/
	
	//Tests that the player is in the correct location
	@Test
	public void humanExistence() {
		
	}
	
	//Tests that the NPC at location (1,1) exists
	@Test
	public void testComputerPlayer1_1() {
		
	}
	
	
	//Tests that the NPC at location () exists
	@Test
	public void testNPC2() {
		
	}
	
	
	//Tests that the NPC at location () exists
	@Test
	public void testNPC3() {
		
	}

	/*************************************************************
	 * Testing on the Deck of Cards 
	 *************************************************************/

	//tests the size of card arrays
	@Test
	public void testCardArraySize() {
		ArrayList<Card> weaponList = board.getWeaponCards();
		assertEquals(6, weaponList.size());
		
		ArrayList<Card> peopleList = board.getPeopleCards();
		assertEquals(6, peopleList.size());

		ArrayList<Card> roomList = board.getRoomCards();
		assertEquals(9, roomList.size());
	}
	
	@Test
	public void testCardTypes() {
		
		ArrayList<Card> weaponList = board.getWeaponCards();
		assertEquals(CardType.WEAPON, weaponList.get(0).getType());
		
		ArrayList<Card> peopleList = board.getPeopleCards();
		assertEquals(CardType.PERSON, peopleList.get(0).getType());

		ArrayList<Card> roomList = board.getRoomCards();
		assertEquals(CardType.ROOM, roomList.get(0).getType());
	}
	
	@Test
	public void testCardNames() {
		//checks that the second weapon in the weapon list is the gun
		ArrayList<Card> weaponList = board.getWeaponCards();
		assertEquals("Gun", weaponList.get(1).getName());
		
		//tests that the last person in cards has the proper name
		ArrayList<Card> peopleList = board.getPeopleCards();
		assertEquals("Harry Potter", peopleList.get(5).getName());

		//tests that the first room in the list of rooms is the correct room
		ArrayList<Card> roomList = board.getRoomCards();
		assertEquals("Children's Room", roomList.get(0).getName());
	}
	
	/*************************************************************
	 * Testing the Solution
	 *************************************************************/
	@Test
	public void testSolution() {
		Solution solution = board.getSolution();
		assertNotNull(solution);
	}

//    Dealing the cards
	
	
}