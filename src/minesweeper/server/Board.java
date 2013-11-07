package minesweeper.server;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Representation of the Minesweeper board. 
 * 
 * Invariants:
 *  - Always equal in width (x) and height (y). 
 *  - Every position can have any of 4 states:
 *      - Untouched
 *      - Flagged
 *      - Dug, for squares with no neighbors with bombs.
 *      - Dug, integer 1-8 (as String), the integer representing 
 *          the # of neighbors with bombs
 *  - A new board must start out in all untouched squares
 *  - A board cannot have a bomb in a location the user has already
 *      dug. If he digs a bomb, he looses and is disconnected. 
 * 
 * @author jains
 *
 */
public class Board {
    
    /**
     * Internally, the board is represented by two boards.
     * USER_BOARD represents the state of the board to the clients.
     * It contains only user relevant states - Untouched, Flagged,
     * and Dug. This is because if a user clicks a bomb, it is removed 
     * and all the Dug squares are updated.
     * 
     * BOMB_BOARD represents the bombs on the board. It must be the same 
     * dimensions as USER_BOARD. Every position on the USER_BOARD corresponds 
     * to the same position on USER_BOARD.  
     */
    
    private final List<List<String>> USER_BOARD = new ArrayList<List<String>>();
    private final List<List<String>> BOMB_BOARD = new ArrayList<List<String>>();
    
    // USER_BOARD SPACE STATES:
    private final String UNTOUCHED = "-";
    private final String FLAGGED = "F";
    private final String DUG_NO_NEIGHBORS = " ";
        
    // BOMB_BOARD SPACE STATES:
    private final String NO_BOMB = "E";
    private final String BOMB = "B";
    
    /**
     * Constructor for a random board. Every square has a 25% 
     *  probability of having a bomb
     * @param size int representing the # of squares on one edge of 
     *  the board.
     */
    public Board(int size) {
        // Initialize the USER_BAORD to all UNTOUCHED squares
        // Initialize BOMB_BOARD squares with bombs with 25% probability
        for (int i = 0; i < size; i++) {
            List<String> newUserLine = new ArrayList<String>();
            List<String> newBombLine = new ArrayList<String>();
            for (int j = 0; j < size; j++){
                newUserLine.add(UNTOUCHED);
                int probability = (int) (Math.random() * 4);
                if (probability == 0)
                    newBombLine.add(BOMB);
                else
                    newBombLine.add(NO_BOMB);
            }
            USER_BOARD.add(newUserLine);
            BOMB_BOARD.add(newBombLine);
        }
        assert checkRep();
    }
    
    /**
     * Constructor for a board from a file. 
     * @param file to read into a board
     * @throws IOException In case file is invalid, or contains malformed input
     */
    public Board(File file) throws IOException{
        
    }
    
    /**
     * Checks rep invariants. 
     *  - USER_BOARD dimentions equal to BOMB_BOARD
     *  - USER_BOARD and BOMB_BOARD contain appropriate states
     * @return boolean true if rep invariants hold, false otherwise. 
     */
    private boolean checkRep() {
        // Check boards have same # of lines. 
        if (USER_BOARD.size() != BOMB_BOARD.size())
            return false;
        for (int i = 0; i < USER_BOARD.size(); i++){
            // Check every line has same number of elements
            if (USER_BOARD.get(i).size() != BOMB_BOARD.get(i).size())
                return false;
            
            for (int j = 0; j < USER_BOARD.size(); j++){
                // Check elements are valid for USER_BOARD
                String userSpace = USER_BOARD.get(i).get(j);
                if ( !(userSpace.equals(UNTOUCHED)) || !(userSpace.equals(FLAGGED)) 
                        || !(userSpace.equals(DUG_NO_NEIGHBORS)) || !(Integer.parseInt(userSpace) < 9) )
                    return false;
                
                // Check elements are valid for BOMB_BOARD
                String bombSpace = BOMB_BOARD.get(i).get(j);
                if( !(bombSpace.equals(BOMB)) || !(bombSpace.equals(NO_BOMB)) )
                    return false;
                
                // Check there is no dug square with a bomb
                if ( (!(userSpace.equals(DUG_NO_NEIGHBORS)) || !(Integer.parseInt(userSpace) < 9))
                    && bombSpace.equals(BOMB) )
                    return false;
            }
        }
        
        return true;
    }
}
