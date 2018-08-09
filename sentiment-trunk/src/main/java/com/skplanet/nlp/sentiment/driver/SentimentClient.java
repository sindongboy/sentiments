package com.skplanet.nlp.sentiment.driver;

import com.skplanet.nlp.cli.CommandLineInterface;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Sample Sentiment Server Client
 *
 * @author Donghun Shin / donghun.shin@sk.com
 *
 */
final class SentimentClient {
	private static final Logger LOGGER = Logger.getLogger(SentimentClient.class.getName());

	public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface();
        cli.addOption("p", "port", true, "port number", true);
        cli.parseOptions(args);
        try {
            int port = Integer.parseInt(cli.getOption("p"));
            new SentimentClient().startClient(port);
        } catch (Exception e) {
            LOGGER.error("Something failed: " + e.getMessage(), e);
        }
    }

	public void startClient(int port) throws IOException {

		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		InetAddress host = null;
		BufferedReader stdIn = null;

		try {
			host = InetAddress.getLocalHost();
            //host = InetAddress.getByName("61.250.47.163");
			socket = new Socket(host.getHostName(), port);

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer;
			String fromUser;

			//Read from socket and write back the response to server. 
			//while ((fromServer = in.readLine()) != null) {
			System.out.print("SENTENCE: ");
			while ((fromUser = stdIn.readLine()) != null) {

				if(fromUser.trim().length() == 0) {
					continue;
				}

				// disconnect client
				if(fromUser.equals("0")) {
					out.println("0");
					LOGGER.info("Client now disconnect!");
					break;
				}

				if(fromUser.equals("-1")) {
					out.println("-1");
					LOGGER.info("stop the server, then disconnect the client");
					break;
				}
				LOGGER.info("Client Sending : " + fromUser);
				out.println(fromUser);

				fromServer = in.readLine();

				// server is down...
				if(fromServer == null) {
					LOGGER.info("Server is not responding, disconnected.");
					break;
				}

				LOGGER.info("Server Responded: " + fromServer);
				System.out.println("Server Responded: " + fromServer);
				System.out.print("comment? (y|n): ");
				String yn = stdIn.readLine();
				StringBuilder comments;
				while(!yn.equals("y") && !yn.equals("n")) {
					System.out.print("comments? (y|n): ");
					yn = stdIn.readLine();
				}

				if(yn.equals("y")) {
					comments = new StringBuilder();
					while((fromUser = stdIn.readLine())!=null) {
						if(fromUser.trim().length() == 0) {
							break;
						}
						comments.append(fromUser).append(" ");
					}
					if(comments.toString().trim().length() > 0) {
						LOGGER.info("COMMENTS: " + comments.toString());
					}
				} 		

				System.out.print("SENTENCE: ");
			}

		} catch (UnknownHostException e) {
            assert host != null;
            System.err.println("Cannot find the host: " + host.getHostName());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't read/write from the connection: " + e.getMessage());
			System.exit(1);
		} finally { //Make sure we always clean up
            assert out != null;
            out.close();
            assert in != null;
            in.close();
            assert stdIn != null;
            stdIn.close();
			socket.close();
		}
	}
}
