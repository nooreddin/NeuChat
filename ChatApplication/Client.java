package ChatApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Collection;
import java.util.Hashtable;

public class Client implements Runnable, Comparable<Client> {
    private final Socket socket;
    private final String username;
    private ChatController controller;

    public void setAcceptPrivate(boolean acceptPrivate) {
        this.acceptPrivate = acceptPrivate;
    }

    public void setAcceptAnon(boolean acceptAnon) {
        this.acceptAnon = acceptAnon;
    }

    private boolean acceptPrivate = true;
    private boolean acceptAnon = true;

    private Room room;
    private Hashtable<Socket,String> blacklist;

    public Client(String username, Socket socket) {
        acceptPrivate = true;
        acceptAnon = true;

        blacklist = new Hashtable<>();
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

    public void block(Client blockedUser) {
        blacklist.put(blockedUser.getSocket(), blockedUser.getUsername());
    }

    public boolean unblock(String username) {
        Client client = Server.instance().getClient(username);
        return client != null && blacklist.remove(client.getSocket(), username);
    }

    public Collection<String> blockedUsers()
    {
        return blacklist.values();
    }

    public boolean isBlocked(Client user1) {
        return blacklist.containsKey(user1.getSocket());
    }

    public boolean getAcceptPrivate() {
        return acceptPrivate;
    }
    public boolean getAcceptAnon() {
        return acceptPrivate;
    }
}
