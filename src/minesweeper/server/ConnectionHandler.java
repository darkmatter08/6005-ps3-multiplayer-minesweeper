package minesweeper.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles a connection for each client. Keeps a reference to the board, and 
 * the socket the client is connected to, as well as the debug settings. 
 * @author jains
 *
 */
public class ConnectionHandler implements Runnable{
    
    private final Socket socket;
    private final boolean debug;
    private final Board b;
    
    // IO
    private BufferedReader in = null;
    private PrintWriter out = null;
    
    public ConnectionHandler(Socket s, boolean debug, Board b){
        this.socket = s;
        this.debug = debug;
        this.b = b;
    }
    
    public void run() {
        // handle the client
        try {
            handleConnection(socket);
        } catch (IOException e) {
            e.printStackTrace(); // but don't terminate serve()
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if connection has an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        out.println("Welcome to Minesweeper. " + b.getNumberOfPlayers() + " people are playing including you. Type 'help' for help.");
        
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (output == null){
                    out.println("Try again. Bad input"); 
                } else if (output.equals("BOOM!\n") && ! debug) {
                    out.println(output); closeConnection(); break; // TODO weird
                } else if (output.equals("")) { // bye case
                    closeConnection(); break;
                } else { 
                    out.println(output);
                }
            }
        } finally {
            b.removePlayer();
            out.close();
            in.close();
            socket.close(); // TODO not sure about this
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private String handleRequest(String input) {
        String regex = "(look)|(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|"
                + "(deflag -?\\d+ -?\\d+)|(help)|(bye)";
        if ( ! input.matches(regex)) {
            // invalid input
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            return b.look();
        } else if (tokens[0].equals("help")) {
            return "That's all the help we offer!";
        } else if (tokens[0].equals("bye")) { // could be disconnected
            // 'bye' request
            return "";
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request // could be disconnected
                return b.dig(x, y);
                // TODO Question 5
            } else if (tokens[0].equals("flag")) {
                return b.flag(x, y);
                // TODO Question 5
            } else if (tokens[0].equals("deflag")) {
                return b.deflag(x, y);
                // TODO Question 5
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
    
    /**
     * Closes the connection, as well as the BufferedReader, and the printWriter.
     */
    private void closeConnection(){
        b.removePlayer();
        try{
            out.close();
            in.close();
            socket.close();
        }catch (IOException e){
            // do nothing, everything is closed
        }
    }
}
