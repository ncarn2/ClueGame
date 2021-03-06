/*
 * Jordan Newport
 * Nicholas Carnival
 */
package clueGame;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public abstract class Player {
	private String playerName;
	private Color color;
	private String colorString;

	protected ArrayList<Card> myCards;
	protected ArrayList<Card> seenCards;
	protected BoardCell location;
	protected BoardCell lastVisited;

	protected Board board;
	protected int dieRoll;
	
	protected Card disprovingCard;

	public Player(String color, String name) {
		this.colorString = color;
		this.playerName = name;
		this.color = convertColor(color);
		myCards = new ArrayList<Card>();
		seenCards = new ArrayList<Card>();
		dieRoll = 0;
		disprovingCard = null;
	}

	// Be sure to trim the color, we don't want spaces around the name 
	public Color convertColor(String strColor) { 
	    Color color;  
	    try {      
	        // We can use reflection to convert the string to a color 
	        Field field = Class.forName("java.awt.Color").getField(strColor.trim()); 
	        color = (Color)field.get(null);  
	  } catch (Exception e) {   
	        color = null; // Not defined   
	  } 
	  return color; 
	} 
	public abstract Card disproveSuggestion(Solution suggestion);

	public void addCard(Card card) {
		seeCard(card);
		this.myCards.add(card);
	}
	public void seeCard(Card card) {
		if (!seenCards.contains(card)) {
			this.seenCards.add(card);
		}
	}
	public void setLocation(BoardCell target) {
		this.location = target;
	}
	
	public BoardCell getLocation() {
		return this.location;
	}

	public String getName() {
		return this.playerName;
	}
	
	public Color getColor() {
		return color;
	}
	public String toString() {
		return ("Name: " + getName() + " Color: " + this.color
				+ "Location: " + getLocation());
		
	}

	public ArrayList<Card> getMyCards() {
		return myCards;
	}
	
	public ArrayList<Card> getSeenCards() {
		return seenCards;
	}
	
	//checks if the player is on top of another player before doing so
	public void movePlayer(BoardCell target) {
		this.location = target;
	}

	public void setBoard(Board board) {
		this.board = board;
	}
	
	public BoardCell getLastVisited() {
		return lastVisited;
	}

	public void setLastVisited(BoardCell lastVisited) {
		this.lastVisited = lastVisited;
	}
	
	public int rollDie() {
		Random random = new Random();
		dieRoll = random.nextInt(6) + 1;
		return dieRoll;
	}
	
	public int getDieRoll() {
		return this.dieRoll;
	}
	
	public String getColorString() {
		return colorString;
	}
	
	public abstract Solution makeAccusation();

}
