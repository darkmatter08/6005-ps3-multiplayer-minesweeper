package minesweeper.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import autograder.TestUtil;

public class BoardTest {
    
    /**
     * Testing Strategy:
     *  - Test Look
     *  - Test dig in many scenarios:
     *      - dig on bomb
     *      - dig on neighbor to 1 bomb
     *      - dig on neighbor to many bombs
     *      - dig on a dug cell, empty and number
     *      - dig recursive
     *      - dig on flag
     *      - dig on unflagged that was previously flagged
     *      - recursive dig shouldn't dig up a flagged square
     *          regardless of having a bomb or not. 
     *      - dig adjacent to flag, expect flag remains
     *  - Flag and Unflag
     *  - Number of players - add, remove
     */
    
    @Test
    public void lookTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            String out = b.look();
            String expected = "- - -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnBombTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(1, 0);
            String out = b.look();
            String expected = "- 1 -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnNeighborToBombTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(2, 0);
            String out = b.look();
            String expected = "- - 1\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnNeighborToManyBombsTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(1, 1);
            String out = b.look();
            String expected = "- - -\r\n- 3 -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnDugEmpty() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.dig(0, 0);
            b.dig(0, 0);
            String out = b.look();
            String expected = "       \r\n  1 1 1\r\n  1 - -\r\n  1 - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnDugNumber() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(1, 1);
            b.dig(1, 1);
            String out = b.look();
            String expected = "- - -\r\n- 3 -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void recursiveDigTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "       \r\n  1 1 1\r\n  1 - -\r\n  1 - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnFlagTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.flag(0, 0);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "F - -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void digOnUnflagTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.flag(0, 0);
            b.dig(0, 0);
            b.deflag(0, 0);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "2 - -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void flagNotDugRecursiveDigTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.flag(1, 1);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "       \r\n  F 1 1\r\n  1 - -\r\n  1 - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }

    @Test
    public void flagWallRecursiveDigTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.flag(2, 0);
            b.flag(2, 1);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "    F -\r\n  1 F -\r\n  1 - -\r\n  1 - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void bigFlagWallRecursiveDigTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.flag(1, 0);
            b.flag(1, 1);
            b.flag(1, 2);
            b.dig(0, 0);
            String out = b.look();
            String expected  = "  F - -\r\n  F - -\r\n  F - -\r\n  1 - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    // tests for subtle bug where we dig a bomb next to a flag. 
    // The following dig should still not dig the flag. 
    @Test 
    public void digBombNextToFlag() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test2.txt"));
            Board b = new Board(file);
            b.flag(1, 2);
            b.dig(2, 2);
            String out = b.look();
            String expected  = "       \r\n       \r\n  F    \r\n       \r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void flagTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.flag(1, 1);
            String out = b.look();
            String expected = "- - -\r\n- F -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void deflagTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.flag(1, 1);
            b.deflag(1, 1);
            String out = b.look();
            String expected = "- - -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ fail("Test did not run"); e.printStackTrace(); }
    }
    
    @Test
    public void playersTest(){
        Board b = new Board(10);
        b.addPlayer();
        assertEquals(b.getNumberOfPlayers(), 1);
        b.addPlayer();
        assertEquals(b.getNumberOfPlayers(), 2);
        b.addPlayer();
        assertEquals(b.getNumberOfPlayers(), 3);
        b.removePlayer();
        assertEquals(b.getNumberOfPlayers(), 2);
    }
}
