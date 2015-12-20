import ChatApplication.ChatController;
import ChatApplication.ClientView;
import ChatApplication.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NeuChat {
    public static void main(String[] args) {
        ClientView clientView = new ClientView();
        Server server = Server.instance();
        server.setClientView(clientView);
        ChatController controller = new ChatController(server, clientView);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(12819);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new Thread(new ConnectionRequest(clientSocket, server, controller)).start();
        }
    }
}
