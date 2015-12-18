package ChatApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client implements Runnable, Comparable<Client> {
    private final Socket socket;
    private final String username;
    private ChatController controller;
    private boolean acceptPrivate = true;
    private boolean acceptAnon = true;

    private Room room;

    public Client(String username, Socket socket) {
        acceptPrivate = true;
        acceptAnon = true;

        this.username = username;
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try

        {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (
                IOException e
                ) {
            e.printStackTrace();
        }
        while (!socket.isClosed()) {
            String message = null;
            try {
                assert br != null;
                message = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message != null)
                controller.fireMessageEvent(this, message);
        }
        controller.fireConnectionEvent(this);
    }


    public void setController(ChatController controller) {
        this.controller = controller;
    }

    public String getUsername() {
        return username;
    }

    public Socket getSocket() {
        return socket;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isAcceptAnon() {
        return acceptAnon;
    }

//    public void setAcceptAnon(boolean acceptAnon) {
//        this.acceptAnon = acceptAnon;
//    }

    public boolean isAcceptPrivate() {
        return acceptPrivate;
    }

//    public void setAcceptPrivate(boolean acceptPrivate) {
//        this.acceptPrivate = acceptPrivate;
//    }


    @Override
    public int compareTo(Client o) {
        return username.compareTo(o.username);
    }

    public String toString() {
        return username;
    }

}
