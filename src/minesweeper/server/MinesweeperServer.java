package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class MinesweeperServer {
    private final ServerSocket serverSocket;
    /**
     * True if the server should _not_ disconnect a client after a BOOM message.
     */
    private final boolean debug;

    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     */
    public MinesweeperServer(int port, boolean debug) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();

            // handle the client
            try {
                handleConnection(socket);
            } catch (IOException e) {
                e.printStackTrace(); // but don't terminate serve()
            } finally {
                socket.close();
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
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (output != null) {
                    out.println(output);
                }
            }
        } finally {
            out.close();
            in.close();
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
            // 'look' request
            // TODO Question 5
        } else if (tokens[0].equals("help")) {
            // 'help' request
            // TODO Question 5
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            // TODO Question 5
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // TODO Question 5
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                // TODO Question 5
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                // TODO Question 5
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }

    /**
     * Start a MinesweeperServer using the given arguments.
     * 
     * Usage: MinesweeperServer [--debug] [--port PORT] [--size SIZE | --file FILE]
     * 
     * The --debug argument means the server should run in debug mode. The server should disconnect
     * a client after a BOOM message if and only if the debug flag argument was NOT given. E.g.
     * "MinesweeperServer --debug" starts the server in debug mode.
     * 
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the
     * server should be listening on for incoming connections. E.g. "MinesweeperServer --port 1234"
     * starts the server listening on port 1234.
     * 
     * SIZE is an optional integer argument specifying that a random board of size SIZE*SIZE should
     * be generated. E.g. "MinesweeperServer --size 15" starts the server initialized with a random
     * board of size 15*15.
     * 
     * FILE is an optional argument specifying a file pathname where a board has been stored. If
     * this argument is given, the stored board should be loaded as the starting board. E.g.
     * "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     * in boardfile.txt, however large it happens to be (but the board may be assumed to be
     * square).
     * 
     * The board file format, for use with the "--file" option, is specified by the following
     * grammar:
     * 
     *   FILE :== LINE+ 
     *   LINE :== (VAL SPACE)* VAL NEWLINE
     *   VAL :== 0 | 1 
     *   SPACE :== " " 
     *   NEWLINE :== "\r?\n"
     * 
     * If neither FILE nor SIZE is given, generate a random board of size 10x10.
     * 
     * Note that FILE and SIZE may not be specified simultaneously.
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = 4444; // default port
        Integer size = 10; // default size
        File file = null;

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        size = Integer.parseInt(arguments.remove());
                        file = null;
                    } else if (flag.equals("--file")) {
                        size = null;
                        file = new File(arguments.remove());
                        if ( ! file.isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file + "\"");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug] [--port PORT] [--size SIZE | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, size, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file. Either the file or the size argument must be null, but not both.
     * 
     * @param debug The server should disconnect a client after a BOOM message if and only if this
     *              argument is false.
     * @param size If this argument is not null, start with a random board of size size * size.
     * @param file If this argument is not null, start with a board loaded from the specified file,
     *             according to the input file format defined in the JavaDoc for main().
     * @param port The network port on which the server should listen.
     */
    public static void runMinesweeperServer(boolean debug, File file, Integer size, int port) throws IOException {
        
        // TODO: Continue your implementation here.
        
        MinesweeperServer server = new MinesweeperServer(port, debug);
        server.serve();
    }
}
