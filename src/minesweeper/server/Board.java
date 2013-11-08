package minesweeper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

import autograder.TestUtil;

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
 * Thread Safety:
 *  - The mutator methods need to be synchronized since we can't 
 *      have multiple mutations occuring in parallel, since we risk
 *      violating the rep invariant of the board
 *  - The accessor methods need to be synchronized so we don't return
 *      a false state of the board - they need to be atomic operations
 *  - We only need to lock on this (instance of Board) and not its
 *      attributes since they are all private, and are only accessed
 *      via the class methods.  
 *  - Every ConnectionHandler is assigned to a client. Since all the 
 *      ConnectionHandlers work on a single instance of the Board, as
 *      long as the Board is threadsafe, the whole server is threadsafe. 
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
    
    private int numberOfPlayers;
    
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
//        Charset charset = Charset.forName("US-ASCII");
//        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), charset)) {
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//        } catch (IOException x) {
//            System.err.format("IOException: %s%n", x);
//        }
        Charset charset = Charset.forName("US-ASCII");
        BufferedReader reader = Files.newBufferedReader(file.toPath(), charset);
        String line = null; 
        while ((line = reader.readLine()) != null) {
            List<String> bombLine = new ArrayList<String>();
            for (char c : line.toCharArray()){
                if (c == '0')
                    bombLine.add(NO_BOMB);
                if (c == '1')
                    bombLine.add(BOMB);
                // do nothing if the char is a space
            }
            BOMB_BOARD.add(bombLine);
        }
        
        // initialize USER_BOARD
        for (int i = 0; i < BOMB_BOARD.size(); i++){
            List<String> userLine = new ArrayList<String>();
            for (int j = 0; j < BOMB_BOARD.size(); j++){
                userLine.add(UNTOUCHED);
            }
            USER_BOARD.add(userLine);
        }
    }
    
    public synchronized String look() {
        String result = "";
        for (List<String> line : USER_BOARD){
            for (String space : line){
                if (space.equals(UNTOUCHED))
                    result += UNTOUCHED;
                else if (space.equals(FLAGGED))
                    result += FLAGGED;
                else if (space.equals(DUG_NO_NEIGHBORS))
                    result += DUG_NO_NEIGHBORS;
                // Must be a number at this point, otherwise rep invariant
                // has been violated. Throws a NumberFormatException (unchecked)
                else {
                    Integer bombHint = Integer.parseInt(space);
                    result += bombHint.toString();
                }
                result += " ";
            }
            // Kill trailing whitespace before adding EOL
            if (result.endsWith(" "))
                result = result.substring(0, result.length()-1);
            result += "\r\n";
        }
        return result;
    }
    
    /**
     * Digs at the location x,y. x,y should have not been dug already
     *  i.e. it should be in the UNTOUCHED or FLAGGED state, otherwise the 
     *  returned string will represent the state of the board. If the 
     *  location contains a bomb, a BOOM message will be returned, and the 
     *  bomb will be removed. Otherwise, the square will be changed to 
     *  DUG. If it has no neighboring bombs, then it will recursively dig
     *  surrounding squares until it reaches a square with a neighboring bomb.
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     * @return a String, representing the state of the board if an invalid
     *  location is given, or a BOOM message if the user hit a bomb. If there
     *  was nothing at that location, then the state of the board is returned. 
     */
    public synchronized String dig(int x, int y) {
        // Get board at position x, y
        String userSquare = USER_BOARD.get(y).get(x);
        assert userSquare.equals(UNTOUCHED) || userSquare.equals(FLAGGED);
        
        if (x < 0 || y < 0)
            return look();
        
        String bombSquare = BOMB_BOARD.get(y).get(x);
        if (bombSquare.equals(BOMB)){
            // also resets the square to DUG_NO_NEIGHBORS
            // and updates the neighboring square's numbers. 
            removeBomb(x, y); 
            dig(x, y);
            return "BOOM!" + "\n";
        }
        else if (bombSquare.equals(NO_BOMB)){
            // Dig the square
            String toSet = findAdjacentBombCount(x, y).toString();
            if (toSet.equals("0")){
                toSet = DUG_NO_NEIGHBORS;
                USER_BOARD.get(y).set(x, toSet);
                recursiveDig(x, y);
            }
            else
                USER_BOARD.get(y).set(x, toSet);
            return look();
        }
        
        // Should never reach here
        throw new RuntimeException();
        
    }
    
    /**
     * Does a recursive dig on all UNTOUCHED neighbor squares as long 
     * as they have no adjacent bombs. The space at x,y should already be 
     * set to DUG_NO_NEIGHBORS.
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     */
    private void recursiveDig(int x, int y) {
        
        Queue<IntPair> toCheck = new LinkedList<IntPair>();
        List<IntPair> checked = new ArrayList<IntPair>();
        
        // add in children of x, y
        for (IntPair childCoord : getChildren(x,y)){
            toCheck.add(childCoord);
        }
        
        // x,y already checked
        checked.add(new IntPair(x, y));
        
        while(! toCheck.isEmpty()){
            IntPair nextCoord = toCheck.poll();
            int i = nextCoord.numerator; // first value
            int j = nextCoord.denominator; // second value
            
            // No bomb, not dug or flagged, and not already checked
            if (BOMB_BOARD.get(j).get(i).equals(NO_BOMB) 
                    && (USER_BOARD.get(j).get(i).equals(UNTOUCHED)
                     || USER_BOARD.get(j).get(i).equals(FLAGGED))
                    && ! checked.contains(new IntPair(i, j))) {
                
                checked.add(new IntPair(i, j));
                // No adjacent bombs - add children
                if ((findAdjacentBombCount(i, j) == 0)){
                    USER_BOARD.get(j).set(i, DUG_NO_NEIGHBORS);
                    for (IntPair childCoord : getChildren(i,j)){
                        if (! checked.contains(childCoord))
                            toCheck.add(childCoord);
                    }
                }
                // don't add children since there are adjacent bombs. 
                // just update the square to be dug with the adjancentBombCount. 
                else
                    USER_BOARD.get(j).set(i, findAdjacentBombCount(i, j).toString());
            }
        }
        
//        int size = USER_BOARD.size();
//        int xC = x - 1;
//        int yC = y - 1;
//        if(xC >= 0 && yC >= 0) { // bounds ok
//        }else if (xC < 0 && yC < 0){
//            xC++;
//            yC++;
//        }else if (yC < 0){
//            yC++;
//        }else if (xC < 0) {
//            xC++;
//        }
//
//        // only check until x+1 or size-1, whichever is smaller
//        for (int i = xC; i <= Math.min(size-1, x+1); i++){ 
//            for (int j = yC; j <= Math.min(size-1, y+1); j++){
//                // No bomb, not dug, and no neighboring bombs
//                if (BOMB_BOARD.get(j).get(i).equals(NO_BOMB) 
//                        && USER_BOARD.get(j).get(i).equals(UNTOUCHED) 
//                        && ! checked.contains(new IntPair(i, j))) {
//                    if ((findAdjacentBombCount(i, j) == 0)){
//                        for (IntPair childCoord : getChildren(i,j)){
//                            toCheck.add(childCoord);
//                        }
//                    }
//                    else {
//                        // change square to findAdjancentBombCount
//                        USER_BOARD.get(j).set(i, findAdjacentBombCount(i, j).toString());
//                    }
//                }
//            }
//        }
    }
    
    private List<IntPair> getChildren(int x, int y) {
        List<IntPair> children = new ArrayList<IntPair>();
        
        int size = USER_BOARD.size();
        int xC = x - 1;
        int yC = y - 1;
        if(xC >= 0 && yC >= 0) { // bounds ok
        }else if (xC < 0 && yC < 0){
            xC++;
            yC++;
        }else if (yC < 0){
            yC++;
        }else if (xC < 0) {
            xC++;
        }

        // only check until x+1 or size-1, whichever is smaller
        for (int i = xC; i <= Math.min(size-1, x+1); i++){ 
            for (int j = yC; j <= Math.min(size-1, y+1); j++){
                children.add(new IntPair(i, j));
            }
        }
        return children;
    }

    /**
     * Finds the space at x,y's bomb hint number
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     * @return Integer 0-8 
     */
    private Integer findAdjacentBombCount(int x, int y) {
        int size = USER_BOARD.size();
        int xC = x - 1;
        int yC = y - 1;
        if(xC >= 0 && yC >= 0) { // bounds ok
        }else if (xC < 0 && yC < 0){
            xC++;
            yC++;
        }else if (yC < 0){
            yC++;
        }else if (xC < 0) {
            xC++;
        }
        
        int bombCount = 0;
        
        // only check until x+1 or size-1, whichever is smaller
        for (int i = xC; i <= Math.min(size-1, x+1); i++){ 
            for (int j = yC; j <= Math.min(size-1, y+1); j++){
                if (BOMB_BOARD.get(j).get(i).equals(BOMB))
                    bombCount++;
            }
        }
        
        return bombCount;
    }

    /**
     * Flags the square at x,y. Requires that x,y be in an untouched state.
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     * @return String representing the state of the board.
     */
    public synchronized String flag(int x, int y) {
        assert USER_BOARD.get(y).get(x).equals(UNTOUCHED);
        USER_BOARD.get(y).set(x, FLAGGED);
        return look();
    }
    
    /**
     * Deflags the square at x,y, returning it to the untouched state.
     *  Requires that x,y already be flagged, and not be in a dug state.
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     * @return String representing the state of the board.
     */
    public synchronized String deflag(int x, int y) {
        assert USER_BOARD.get(y).get(x).equals(FLAGGED);
        USER_BOARD.get(y).set(x, UNTOUCHED);
        return look();
    }
    
    /**
     * Removes the bomb from the location x,y, and updates all adjacent squares'
     * 'bomb hint' number. Location x,y must have a bomb. 
     * @param x int x coord. x >= 0
     * @param y int y coord. y >= 0
     */
    private synchronized void removeBomb(int x, int y){
        assert BOMB_BOARD.get(y).get(x).equals(BOMB);
        BOMB_BOARD.get(y).set(x, NO_BOMB);
        
        String toSet = findAdjacentBombCount(x, y).toString();
        if (toSet.equals("0")){
            toSet = DUG_NO_NEIGHBORS;
        }
        USER_BOARD.get(y).set(x, toSet);
        
        int size = USER_BOARD.size();
        int xC = x - 1;
        int yC = y - 1;
        if(xC >= 0 && yC >= 0) { // bounds ok
        }else if (xC < 0 && yC < 0){
            xC++;
            yC++;
        }else if (yC < 0){
            yC++;
        }else if (xC < 0) {
            xC++;
        }
        
        for (int i = xC; i <= Math.min(size-1, x+1); i++){ // only check until x+1 or size-1
            for (int j = yC; j <= Math.min(size-1, y+1); j++){
                String toSet1 = findAdjacentBombCount(i, j).toString();
                if (toSet1.equals("0")){
                    toSet1 = DUG_NO_NEIGHBORS;
                }
                if (! (USER_BOARD.get(j).get(i).equals(UNTOUCHED)))
                    USER_BOARD.get(j).set(i, toSet1);
            }
        }
    }
    
    /**
     * Checks rep invariants. 
     *  - USER_BOARD Dimensions equal to BOMB_BOARD
     *  - USER_BOARD and BOMB_BOARD contain appropriate states
     * @return boolean true if rep invariants hold, false otherwise. 
     */
    private synchronized boolean checkRep() {
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
    
    public synchronized void addPlayer() {
        numberOfPlayers++;
    }
    
    public synchronized void removePlayer() {
        numberOfPlayers--;
    }
    
    public synchronized int getNumberOfPlayers() {
        return numberOfPlayers;
    }
}
