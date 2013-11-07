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
     *      - dig recursive
     */
    
    @Test
    public void lookTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            String out = b.look();
            String expected = "- - -\r\n- - -\r\n- - -\r\n";
            assertEquals(out, expected);
        }catch (IOException e){ /* do nothing */ }
    }
    
    @Test
    public void digOnBombTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(1, 0);
            String out = b.look();
            String expected = "- 1 -\r\n- - -\r\n- - -\r\n";
//            System.out.println(out);
//            System.out.println(expected);
            assertEquals(out, expected);
        }catch (IOException e){ /* do nothing */ }
    }
    
    @Test
    public void digOnNeighborToBombTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(2, 0);
            String out = b.look();
            String expected = "- - 1\r\n- - -\r\n- - -\r\n";
//            System.out.println(out);
//            System.out.println(expected);
            assertEquals(out, expected);
        }catch (IOException e){ /* do nothing */ }
    }
    
    @Test
    public void digOnNeighborToManyBombsTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/test.txt"));
            Board b = new Board(file);
            b.dig(1, 1);
            String out = b.look();
            String expected = "- - -\r\n- 3 -\r\n- - -\r\n";
//            System.out.println(out);
//            System.out.println(expected);
            assertEquals(out, expected);
        }catch (IOException e){ /* do nothing */ }
    }
    
    @Test
    public void recursiveDigTest() {
        try{
            File file = new File(TestUtil.getResourcePathName("autograder/resources/board_file_5"));
            Board b = new Board(file);
            b.dig(0, 0);
            String out = b.look();
            String expected = "- - -\r\n- 3 -\r\n- - -\r\n";
            System.out.println(out);
            System.out.println(expected);
            assertEquals(out, expected);
        }catch (IOException e){ /* do nothing */ }
    }

}
