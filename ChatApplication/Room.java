package ChatApplication;

import java.net.InetAddress;
import java.util.*;


public class Room implements Comparable<Room> {
    public static final int SUCCESS = 0;
    public static final int FULL_ROOM = 1;
    public static final int BANNED_USER = 2;
    private static final int MAX_CAPACITY = 40;
    private static final Object USERS_LOCK = new Object();
    private static Room publicRoom;
    private final String name;
    private Client owner;
    private Hashtable<String, Client> users;
    private int size;
    private List<InetAddress> bannedAddresses;
    private List<String> bannedUsernames;


    public Room(Client owner, String name) {

        this.owner = owner;
        this.name = name;
        size = 1;

        users = new Hashtable<>();
        users.put(owner.getUsername(), owner);

        bannedAddresses = new LinkedList<>();
        bannedUsernames = new LinkedList<>();
    }

    /**
     * @param name The name of the public room in the chat server
     * @return returns the public room
     */
    public static Room getPublicRoom(String name) {
        if (publicRoom == null) {
            publicRoom = new Room(new Client("", null), name);
            // Public room doen't have any owners
            publicRoom.clearAll();
            publicRoom.size = 0;
        }

        return publicRoom;
    }

    public String getName() {
        return name;
    }

    public Client getOwner() {
        return owner;
    }


    public int addUser(Client user) {
        synchronized (USERS_LOCK) {
            if (isFull())
                return FULL_ROOM;

            else if (isBanned(user))
                return BANNED_USER;

            users.put(user.getUsername(), user);
            user.setRoom(this);

            size++;
            return SUCCESS;
        }
    }

    public Client getUser(String username) {
        return users.get(username);
    }

    public Enumeration<Client> users() {
        return users.elements();
    }

    public Collection<? extends Client> userSet() {
        return users.values();
    }

//    public Enumeration<String> usernames()
//    {
//        return users.keys();
//    }
//
//    public Set<String> usernameSet()
//    {
//        return users.keySet();
//    }

    /**
     * Removes the specific user from room, if the user is owner it will not effect the removal
     *
     * @param user the user to be removes
     */
    public void removeUser(Client user) {
        synchronized (USERS_LOCK) {
            if (user == owner)
                return;

            if (user.getRoom() == this) {
                user.setRoom(null);
                users.remove(user.getUsername());
                size--;
            } else if (users.containsKey(user.getUsername())) {
                users.remove(user.getUsername());
                size--;
            }

        }
    }

    /**
     * Removes all the users of this room (not including the owner)
     */
    public void removeAll() {
        Enumeration<Client> userEnumeration = users.elements();
        while (userEnumeration.hasMoreElements())
            removeUser(userEnumeration.nextElement());
        users.clear();
    }

    /**
     * clears the room and prepares it for garbage collection
     */
    public void clearAll() {
        removeAll();
        owner.setRoom(null);
        owner = null;
    }

    public boolean banUser(Client user) {
        synchronized (USERS_LOCK) {
            bannedAddresses.add(user.getSocket().getInetAddress());
            bannedUsernames.add(user.getUsername());
            removeUser(user);
        }
        return false;
    }

    public Iterator<String> bannedListIter() {
        return bannedUsernames.iterator();
    }


    public boolean unbanUser(String username) {
        synchronized (USERS_LOCK) {
            boolean found = false;
            int index = bannedUsernames.indexOf(username);
            while (index >= 0) {
                found = true;
                bannedAddresses.remove(index);
                bannedUsernames.remove(index);
                index = bannedUsernames.indexOf(username);
            }
            return found;
        }
    }

    public void unbanAll() {
        bannedUsernames = new LinkedList<>();
        bannedAddresses = new LinkedList<>();
    }

    public boolean isBanned(Client user) {
        return bannedAddresses.contains(user.getSocket().getInetAddress());
    }


    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return MAX_CAPACITY;
    }

    private boolean isFull() {
        return size == MAX_CAPACITY;
    }


    @Override
    public int compareTo(Room o) {
        return name.compareToIgnoreCase(o.name);
    }

    public String toString() {
        return name;
    }
}
