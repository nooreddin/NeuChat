import ChatApplication.ChatController;
import ChatApplication.Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionRequest implements Runnable {
    private Server server;
    private ChatController controller;
    private Socket clientSocket;

    public ConnectionRequest(Socket clientSocket, Server server, ChatController controller) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.controller = controller;
    }

    public void run() {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outputWriter = new DataOutputStream(clientSocket.getOutputStream());

            outputWriter.writeChars("\nWelcome to NeuChat chat server.\n");
            String username;
            boolean finishTries = false;
            for (int i = 0; i < 10; i++) {
                outputWriter.writeChars("Login Name? ");
                username = inputReader.readLine();
                if (server.registerClient(username, clientSocket, controller)) {
                    outputWriter.writeChars("Welcome " + username + "\n");
                    outputWriter.writeChars("At any time type /help to have access to a list of available commands.\n");
                    finishTries = true;
                    break;
                } else
                    outputWriter.writeChars("Sorry, name taken.\n");

            }

            if (!finishTries) {
                outputWriter.writeChars("Attempted more than 10 times. Try again later.\n");
                outputWriter.writeChars("BYE\n");
                clientSocket.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
