package ChatApplication;


import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Server {
    private static Server server;
    private final Object CLIENT_LOCK = new Object();
    private final Object ROOM_LOCK = new Object();
    private ClientView clientView;
    private Hashtable<String, Client> clients;
    private Hashtable<String, Room> rooms;

    private Server() {
        clients = new Hashtable<>();
        rooms = new Hashtable<>();
        rooms.put("public", Room.getPublicRoom("Public"));
    }

    public static Server instance() {
        if (server == null)
            server = new Server();

        return server;
    }

    public void setClientView(ClientView clientView) {
        this.clientView = clientView;
    }


    public boolean registerClient(String username, Socket socket, ChatController chatController) {
        synchronized (CLIENT_LOCK) {
            if (clients.containsKey(username))
                return false;
            Client client = new Client(username, socket);
            clients.put(username, client);
            client.setController(chatController);
            new Thread(client).start();

            return true;
        }
    }

    public void unregisterClient(Client client) {
        synchronized ((CLIENT_LOCK)) {
            clientView.sendMessage(client, "BYE");
            server.leaveRoom(client);

            try {
                client.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            clients.remove(client.getUsername());
        }
    }

    public void makeRoom(Client client, String roomName) {
        synchronized (ROOM_LOCK) {
            if (client.getRoom() != null)
                clientView.sendErrorMessage(client, "you need to first leave room: " + client.getRoom());
            else if (rooms.containsKey(roomName.toLowerCase()))
                clientView.sendErrorMessage(client, "the room is already exist:" + roomName);
            else {
                Room room = new Room(client, roomName);
                synchronized (client) {
                    if (client.getRoom() == null) {
                        client.setRoom(room);
                        rooms.put(roomName.toLowerCase(), room);
                        clientView.sendFeedbackMessage(client, "successfully created the room: " + room);
                    } else {
                        clientView.sendErrorMessage(client, "you need to first leave room: " + client.getRoom());
                    }
                }
            }
        }
    }

    private void removeRoom(Room room) {
        rooms.remove(room.getName().toLowerCase());
    }

    public Collection<Room> roomSet() {
        return rooms.values();
    }

    public void joinRoom(Client user, String roomName) {
        Room room = rooms.get(roomName.toLowerCase());
        if (room == null)
            clientView.sendErrorMessage(user, "Room is not exist: " + roomName);
        else
            joinRoom(user, room);
    }

    private void joinRoom(Client user, Room room) {
        synchronized (user) {
            if (user.getRoom() != null)
                clientView.sendErrorMessage(user, "cannot join to two rooms at " +
                        "the same time you are aleady in one room: " + user.getRoom());
            else {
                int insertStatus = room.addUser(user);
                if (insertStatus == Room.SUCCESS) {
                    clientView.sendMessage(user, "Entering room: " + room.getName());
                    List<Client> users = new LinkedList<>(room.userSet());
                    Collections.sort(users);
                    Iterator<Client> userIter = users.iterator();

                    while ((userIter.hasNext())) {
                        Client thisUser = userIter.next();
                        if (thisUser == user)
                            clientView.sendMessage(user, "* " + thisUser + " (** this is you)");
                        else
                            clientView.sendMessage(user, "* " + thisUser);
                    }
                    clientView.sendMessage(user, "end of list.");

                    Enumeration<Client> messageList = room.users();

                    while (messageList.hasMoreElements()) {
                        Client thisUser = messageList.nextElement();
                        if (thisUser != user)
                            clientView.sendChatMessage(thisUser, "* new user joined chat: " + user);
                    }
                } else if (insertStatus == Room.FULL_ROOM)
                    clientView.sendErrorMessage(user, "Room is full capacity: " + room + "(" + room.getCapacity() + ")");
                else if (insertStatus == Room.BANNED_USER)
                    clientView.sendErrorMessage(user, "You are banned for this room: " + room);
            }
        }
    }

    public void leaveRoom(Client user) {
        leaveRoom(user, user.getRoom());
    }

    private void leaveRoom(Client user, Room room) {
        synchronized (user) {
            if (user.getRoom() == null)
                return;
            if (user == room.getOwner()) {
                clientView.sendFeedbackMessage(user, "user has left chat: "
                        + user + "(" + "** this is you" + ")");
                clientView.sendFeedbackMessageToAll(room.users(), "owner left the room, the chat is closed: " + room);
                room.clearAll();
                removeRoom(room);
            } else if (user.getRoom() == room) {
                clientView.sendFeedbackMessage(user, "user has left chat: "
                        + user + "(" + "** this is you" + ")");
                room.removeUser(user);
                clientView.sendFeedbackMessageToAll(room.users(), "user has left chat: " + user);
            }
        }
    }


    public void privateMessage(Client user1, String username2, String message) {

        synchronized (user1) {
            Room room = user1.getRoom();
            if (room == null) {
                clientView.sendErrorMessage(user1, "You need to be inside a room to send private message.");
                return;
            }
            Client user2 = room.getUser(username2);
            if (user2 == null) {
                clientView.sendErrorMessage(user1, "the user \"" + username2 + "\" is not in this room.");
            } else if(user2.isBlocked(user1))
            {
                clientView.sendErrorMessage(user1, "User is blocked you: " + user2);

            }
            else if(user2.getAcceptPrivate())
            {
                privateMessage(user1, user2, message);
            }
            else
            {
                clientView.sendErrorMessage(user1, "User does not accept private messages.");
            }
        }
    }

    public void privateMessage(Client user1, Client user2, String message) {
        if (user2.isAcceptPrivate())
            clientView.sendChatMessage(user2, "PRV " + user1 + " to " + user2 + ": " + message);
        else
            clientView.sendFeedbackMessage(user1, "user does not accept private messages.");

    }

    public void anonMessage(Client user1, String username2, String message) {
        synchronized (user1) {
            Room room = user1.getRoom();
            if (room == null) {
                clientView.sendErrorMessage(user1, "You need to be inside a room to send anonymous message.");
                return;
            }
            Client user2 = room.getUser(username2);
            if (user2 == null) {
                clientView.sendErrorMessage(user1, "the user \"" + username2 + "\" is not in this room.");
            } else if(user2.getAcceptAnon()){
                anonMessage(user1, user2, message);
            }
            else
            {
                clientView.sendErrorMessage(user1, "User does not accept anonymous messages.");
            }
        }
    }

    private void anonMessage(Client user1, Client user2, String message) {
        if (user2.isAcceptAnon())
            clientView.sendChatMessage(user2, "ANON to " + user2 + ": " + message);
        else
            clientView.sendFeedbackMessage(user1, "user does not accept anonymous messages.");
    }

    public void publicMessage(Client user, String message) {
        Room room = user.getRoom();
        if (room != null)
            clientView.sendChatMessageToAll(room.users(), user + ": " + message);
        else
            clientView.sendErrorMessage(user, "You need to be inside a room to send message.");
    }


    public void banUser(Client user, String username2) {
        synchronized (CLIENT_LOCK) {
            Room room = user.getRoom();
            Client bannedUser = room.getUser(username2);
            if (room.getOwner() == user) {
                if (bannedUser == user)
                    clientView.sendErrorMessage(bannedUser, "Owner cannot be banned: " + user);
                else if (bannedUser != null) {
                    clientView.sendChatMessage(bannedUser, "Owner banned you from the room: " + room);
                    room.banUser(bannedUser);
                    clientView.sendChatMessageToAll(room.users(), "User is banned: " + username2);
                } else
                    clientView.sendErrorMessage(user, "There is no such a user at room: " + username2);
            } else
                clientView.sendErrorMessage(user, "You have to be the owner of the room: " + room);
        }
    }

    public void unbanUser(Client user, String username2) {
        synchronized (CLIENT_LOCK) {
            Room room = user.getRoom();
            if (room.getOwner() == user) {
                if (room.unbanUser(username2))
                    clientView.sendFeedbackMessage(user, "User is unbanned: " + username2);
                else
                    clientView.sendErrorMessage(user, "There is no such a user at banned list: " + username2);
            } else
                clientView.sendErrorMessage(user, "You have to be the owner of the room: " + room);
        }
    }

    public void unbanAll(Client user) {
        synchronized (CLIENT_LOCK) {
            Room room = user.getRoom();
            if (room.getOwner() == user) {
                room.unbanAll();
                clientView.sendChatMessage(user, "All banned clients are unbanned");
            } else
                clientView.sendErrorMessage(user, "You have to be the owner of the room: " + room);
        }
    }

    public void showBanned(Client user) {
        if (user.getRoom().getOwner() == user)
            clientView.showBanned(user);
        else
            clientView.sendErrorMessage(user, "You need to be owner to have access to the banned list: " + user.getRoom());

    }

    public void blockUser(Client client, String s) {
        synchronized (CLIENT_LOCK) {
            Room room = client.getRoom();
            Client blockedUser = room.getUser(s);
            if (blockedUser == null || blockedUser.getRoom() != room)
                clientView.sendErrorMessage(client, "User is not in the room: " + s);
            else
            {
                client.block(blockedUser);
                clientView.sendFeedbackMessage(client, "User is blocked: " + s);
                clientView.sendChatMessage(blockedUser, "User blocked you: " + client);
            }
        }
    }

    public void unblockUser(Client client, String s) {
        synchronized (CLIENT_LOCK) {
            if(client.unblock(s))
                clientView.sendFeedbackMessage(client, "User is unblocked: " + s);
            else
                clientView.sendErrorMessage(client, "User is not in the blocked list: " + s);
        }
    }

    public void showBlocked(Client client) {
        Collection<String> clients = client.blockedUsers();
        clientView.sendFeedbackMessage(client, "Blocked user list:\n");
        for (String blocketUser : clients)
            clientView.sendMessage(client, blocketUser + "\n");
        clientView.sendFeedbackMessage(client, "End of list:\n");
    }

    public Client getClient(String username) {
        return clients.get(username);
    }

    public void setPrivate(Client client, String status) {
        client.setAcceptPrivate(status.equalsIgnoreCase("on"));
        clientView.sendFeedbackMessage(client, "Private messages are: " + status.toUpperCase());
    }

    public void setAnon(Client client, String status) {
        client.setAcceptAnon(status.equalsIgnoreCase("on"));
        clientView.sendFeedbackMessage(client, "Anonymous messages are: " + status.toUpperCase());
    }

    public void closedConnectionPerformed(Client client) {
        Room room = client.getRoom();
        synchronized ((CLIENT_LOCK)) {
                if (client.getRoom() == null)
                    return;
                if (client == room.getOwner()) {

                    Enumeration<Client> clients = room.users();
                    while (clients.hasMoreElements()) {
                        Client thisUser = clients.nextElement();
                        if(thisUser != client)
                            clientView.sendFeedbackMessage(thisUser, "owner left the room, the chat is closed: " + room);
                    }
                    room.clearAll();
                    removeRoom(room);
                } else if (client.getRoom() == room) {
                    room.removeUser(client);
                    clientView.sendFeedbackMessageToAll(room.users(), "user has left chat: " + client);
                }


            try {
                client.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            clients.remove(client.getUsername());
        }
    }
}
