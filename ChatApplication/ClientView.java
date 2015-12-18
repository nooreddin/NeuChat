package ChatApplication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class ClientView {
    public void displayHelp(Client client) {
        String helpMssage = "List of usable commands:\n";
        helpMssage += "Command\t\t\tAction\n";
        helpMssage += "/help\t\t\tDisplays this menu.\n";
        helpMssage += "/stat\t\t\tDisplays the user information.\n";
        helpMssage += "/rooms\t\t\tDisplay the name of the available rooms.\n";
        helpMssage += "/join room\t\tJoin to the room.\n";
        helpMssage += "/prv user message\tSend a private message to another user.\n";
        helpMssage += "/anon user message\tSend an anonymous message to another user.\n";
        helpMssage += "/ban user\t\tBan a specific user.\n";
        helpMssage += "/unban user\t\tUnban a specific user.\n";
        helpMssage += "/unbanall\t\tUnban all banned users.\n";
        helpMssage += "/banned\t\t\tDisplay the list of banned users.\n";
        helpMssage += "/leave\t\t\tLeave the current chat room\n";
        helpMssage += "/newroom room\t\tCreate a new room.\n";
        helpMssage += "/quit\t\t\tQuit from the chat server.\n";
        helpMssage += "End of list";

        sendMessage(client, helpMssage);
    }

    public void showRooms(Client client) {
        sendMessage(client, "Active rooms are:");
        Collection<Room> rooms = Server.instance().roomSet();
        LinkedList<Room> list = new LinkedList<>(rooms);
        Collections.sort(list);
        for (Room room : list)
            sendMessage(client, "* " + room + " (" + room.getSize() + ")");

        sendMessage(client, "End of list.");
    }

    public void sendMessage(Client client, String message) {

        try {
            DataOutputStream writer = new DataOutputStream(client.getSocket().getOutputStream());

            writer.writeChars(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToAll(Enumeration<Client> clients, String message) {
        while (clients.hasMoreElements())
            sendMessage(clients.nextElement(), message);
    }

    public void sendErrorMessage(Client client, String message) {
        sendMessage(client, "Error: " + message);
    }

    public void sendFeedbackMessage(Client client, String message) {
        sendMessage(client, ">> " + message);
    }

    public void sendFeedbackMessageToAll(Enumeration<Client> clients, String message) {
        while (clients.hasMoreElements())
            sendFeedbackMessage(clients.nextElement(), message);
    }

    public void showBanned(Client client) {
        Room room = client.getRoom();

        Iterator<String> bannedUserIter = room.bannedListIter();

        sendMessage(client, "Banned list are:");

        while (bannedUserIter.hasNext())
            sendMessage(client, "* " + bannedUserIter.next());

        sendMessage(client, "end of list.");

    }

    public void showStatus(Client client) {
        String status = "Status:\n";
        status += "Username:\t" + client.getUsername() + "\n";
        status += "Room:\t\t" + (client.getRoom() == null ? "Not in a room" : client.getRoom()) + "\n";
        if( client.getRoom() != null )
            status += "*Privilege:\t" + (client.getRoom().getOwner() == client ? "Owner\n" : "User\n");
        status += "Address:\t" + client.getSocket().getInetAddress() + "\n";
        sendFeedbackMessage(client, status);
    }
}
