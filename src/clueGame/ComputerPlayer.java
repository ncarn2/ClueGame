/*
 * Jordan Newport
 * Nicholas Carnival
 */
package clueGame;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ComputerPlayer extends Player {

	private BoardCell location;
	private BoardCell lastVisited;

	public ComputerPlayer(String color, String name) {
		super(color, name);
	}
	public BoardCell pickLocation(Set<BoardCell> targets) {
		BoardCell currentCell = null;
		for(BoardCell b : targets) {
			if(b.isDoorway()) {
				currentCell = b;
				this.lastVisited = currentCell;
				break;
			}
		}
		//if there are no rooms
		if(currentCell == null) {
			BoardCell randomCell = null;
			int randomInt = new Random().nextInt(targets.size());
			int i = 0;
			for(BoardCell cell : targets) {
				if(i == randomInt) {
					randomCell = cell;
					this.lastVisited = randomCell;
					break;
				}
				i++;
			}
			currentCell = randomCell;
		}
		this.location = currentCell;
		return currentCell;
	}
	public BoardCell getLocation() {
		return this.location;
	}
	public void makeAccusation() {
		
	}
	public void createSuggestion() {
		
	}

	public Card disproveSuggestion(Solution suggestion) {
		return new Card();
	}

}
