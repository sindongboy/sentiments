package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.cli.CommandLineInterface;
import com.skplanet.nlp.sentiment.analyzer.SentimentAnalyzer;
import com.skplanet.nlp.sentiment.ds.Sentiment;
import com.skplanet.nlp.sentiment.knowledge.SentimentDict;
import com.skplanet.nlp.sentiment.util.NLPUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * SentimentAnalyzer Server Sample program.
 *
 * @author Donghun Shin / donghun.shin@sk.com
 */
final class SentimentServer {
	// logger
	private static final Logger LOGGER = Logger.getLogger(SentimentServer.class.getName());

	// nlp

    // Sentiment Analyzer
    private SentimentAnalyzer analyzer = null;

	/**
	 * Socket main
	 *
	 * @param args args 0 - port num
	 */
	public static void main(String args[]) {
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("c", "category", true, "category number", true);
        cli.addOption("p", "port", true, "port number", true);
        cli.parseOptions(args);

        String category = cli.getOption("c");
        int port = Integer.parseInt(cli.getOption("p"));
        SentimentServer server = new SentimentServer(category, port);
        server.startServer();
    }

	// declare a server socket and a client socket for the server;
	// declare the number of connections

	private ServerSocket echoServer = null;
	private Socket clientSocket = null;
	private int numConnections = 0;
	private int port;

	/**
	 * Sentiment Server Constructor
	 *
	 * @param port port number
	 */
	public SentimentServer( String category, int port ) {
		LOGGER.info("Sentiment Configuration loading ...");
		LOGGER.info("NLP loading ...");
		LOGGER.info("Sentiment Dictionary loading ...");
        SentimentDict dict = new SentimentDict(NLPUtils.getInstance());
		dict.load(category);
        this.analyzer = new SentimentAnalyzer(dict);
        this.port = port;
	}

	/**
	 * Stop Server
	 *
	 */
	public void stopServer() {
		LOGGER.info("Server cleaning up.");
		System.exit(0);
	}

	/**
	 * Start Server
	 *
	 */
	public void startServer() {
		try {
			echoServer = new ServerSocket(port);
		}
		catch (IOException e) {
			LOGGER.error(e);
		}   

		LOGGER.info("Sentiment API Server is now started and is waiting for connections.");
		//System.out.println( "Any client can send -1 to stop the server." );

		// waiting for connections
		while ( true ) {
			try {
				clientSocket = echoServer.accept();
				numConnections ++;
				ServerConnection oneconnection = new ServerConnection(clientSocket, numConnections, this, this.analyzer);
				new Thread(oneconnection).start();
			}   
			catch (IOException e) {
				LOGGER.error(e);
			}
		}
	}
}

/**
 * Business Logic
 * Server Connection , Multi-thread implementation
 *
 * @since 2014. 02. 25
 * @author Donghun, Shin @ SK planet 
 */
class ServerConnection implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(ServerConnection.class.getName());

	private BufferedReader is;
	private PrintStream os;
	private Socket clientSocket;
	private int id;
	private SentimentServer server;

	private SentimentAnalyzer analyzer = null;
	private NLPUtils nlp = NLPUtils.getInstance();

	/**
	 * Connection Constructor
	 *
	 * @param clientSocket client socket
	 * @param id id
	 * @param server server
	 */
	public ServerConnection(Socket clientSocket, int id, SentimentServer server, SentimentAnalyzer analyzer) {
        this.analyzer = analyzer;
        this.clientSocket = clientSocket;
        this.id = id;
        this.server = server;
        LOGGER.info("Connection " + id + " established with: " + clientSocket);
        try {
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            os = new PrintStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

	/**
	 * {@inheritDoc}
	 * @see Runnable#run()
	 */
	public void run() {
		StringBuilder resultString;
		String line;
		try {
			boolean serverStop = false;

			while (true) {
				line = is.readLine();

				// stop server
				if ( line.equals("-1") ) {
					serverStop = true;
					break;
				}

				// disconnect client
				if ( line.equals("0") ) {
					break;
				}

				LOGGER.info("Received " + line + " from Connection " + id + ".");


				// -------------- // 
				// Analysis
				// * output format
				// SENTENCE^REP,ATT,EXP,POL:REP2,ATT2,EXP2,POL2:...||SENTENCE2^REP,ATT,EXP,POL:REP2,ATT2,EXP2,POL2:...
				// -------------- // 
				resultString = new StringBuilder();
				String[] sentences = this.nlp.getSentences(line);
				
				// for each lines
				for(int linenum = 0; linenum < sentences.length; linenum++) {
					LOGGER.debug("ANAL. SENT: " + sentences[linenum]);
					List<Sentiment> sentiments = this.analyzer.find(sentences[linenum]);
					if(sentiments.size() > 0) {
						resultString.append(sentences[linenum]).append("^");
						for(int sentnum = 0; sentnum < sentiments.size(); sentnum++) {
							if(sentnum == sentiments.size() - 1) {
								resultString.append(sentiments.get(sentnum).getExpression().getRepAttribute()).append(",")
									.append(sentiments.get(sentnum).getAttribute().getText()).append(",")
									.append(sentiments.get(sentnum).getExpression().getText()).append(",")
									.append(sentiments.get(sentnum).getExpression().getValue());
							} else {
								resultString.append(sentiments.get(sentnum).getExpression().getRepAttribute()).append(",")
									.append(sentiments.get(sentnum).getAttribute().getText()).append(",")
									.append(sentiments.get(sentnum).getExpression().getText()).append(",")
									.append(sentiments.get(sentnum).getExpression().getValue()).append(":");
							}
						}
					}
					if(linenum != sentences.length - 1) {
						resultString.append("||");
					} 
				}

				if(line.trim().length() == 0) {
					os.println("NULL");
				} else {
					os.println(resultString.toString()); 
				}
			}

			LOGGER.info("Connection " + id + " closed.");
			is.close();
			os.close();
			clientSocket.close();

			// stop the server
			if ( serverStop ) {
				server.stopServer();
			}

		} catch (IOException e) {
			LOGGER.error("IOException occurred", e);
		}
	}
}
