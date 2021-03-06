/*
 * Jordan Newport
 * Nicholas Carnival
 */
package clueGame;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

public class HumanPlayer extends Player {
	
	private boolean hasMoved;
	private boolean isTurn;
	private boolean makingSuggestion;

	public HumanPlayer(String color, String name) {
		super(color, name);
		hasMoved = false;
	}

	public Solution createSuggestion() {
		Solution suggestion = new Solution();
		
		Random random = new Random();
		// set person to one we haven't seen
		ArrayList<Card> people = new ArrayList<Card>();
		for(Card c : board.getPeopleCards()) {
			if(!seenCards.contains(c)) {
				people.add(c);
			}
		}
		suggestion.setPerson(people.get(random.nextInt(people.size())));

		// set weapon to one we haven't seen
		ArrayList<Card> weapons = new ArrayList<Card>();
		for(Card c : board.getWeaponCards()) {
			if(!seenCards.contains(c)) {
				weapons.add(c);
			}
		}
		suggestion.setWeapon(weapons.get(random.nextInt(weapons.size())));

		// set room to one we're in
		char currentRoom = location.getInitial();
		String room = board.getLegend().get(currentRoom);
		for (Card c : board.getRoomCards()) {
			if (c.getName().equals(room)) {
				suggestion.setRoom(c);
				break;
			}
		}
		return suggestion;
	}
	
	public void setLocation(BoardCell cell) {
		this.location = cell;
	}
	
	public Boolean canChangeTurn() {
		return hasMoved;
	}

	//disproves the suggested card
	public Card disproveSuggestion(Solution suggestion) {
		Random random = new Random();
		ArrayList<Card> matches = new ArrayList<Card>();
		if(myCards.contains(suggestion.getRoom())) {
			matches.add(suggestion.getRoom());
		}
		if(myCards.contains(suggestion.getPerson())) {
			matches.add(suggestion.getPerson());
		}
		if(myCards.contains(suggestion.getWeapon())) {
			matches.add(suggestion.getWeapon());
		}
		if(matches.size() > 0) {
			return matches.get(random.nextInt(matches.size()));
		} else {
			return null;
		}
	}

	public Boolean isTurn() {
		return isTurn;
	}

	public void setIsTurn(Boolean b) {
		this.isTurn = b;
	}

	public Boolean hasMoved() {
		return this.hasMoved;
	}

	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}
	
	public Solution makeAccusation() {
		return new Solution();
	}
	
}
