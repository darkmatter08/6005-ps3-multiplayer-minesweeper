package autograder;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.junit.Before;
import org.junit.Test;

/**
 * Ensures that students got the sense of X,Y directions right.
 */
public class PublishedTest {
    @Before
    public void setUp() {
        TestUtil.startServer(true, "board_file_5");
    }

    @Test(timeout = 10000)
    public void publishedTest() throws IOException, InterruptedException {
        // Avoid race where we try to connect to server too early
        Thread.sleep(100);

        try {
            Socket sock = TestUtil.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    sock.getInputStream()));
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

            assertEquals(true, TestUtil.nextNonEmptyLine(in).startsWith("Welcome"));
            // This particular test ignores extraneous newlines; other tests may not.
            out.println("look");
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            out.println("dig 3 1");

            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - 1 - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            assertEquals("- - - - - - -", TestUtil.nextNonEmptyLine(in));
            out.println("dig 4 1");
            assertEquals("BOOM!", TestUtil.nextNonEmptyLine(in));
            out.println("look"); // Debug is true.
            assertEquals("             ", TestUtil.nextNonEmptyLine(in));
            assertEquals("             ", TestUtil.nextNonEmptyLine(in));
            assertEquals("             ", TestUtil.nextNonEmptyLine(in));
            assertEquals("             ", TestUtil.nextNonEmptyLine(in));
            assertEquals("             ", TestUtil.nextNonEmptyLine(in));
            assertEquals("1 1          ", TestUtil.nextNonEmptyLine(in));
            assertEquals("- 1          ", TestUtil.nextNonEmptyLine(in));
            out.println("bye");
            sock.close();
        } catch (SocketTimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
