package ChatApplication;


import java.util.StringTokenizer;
import java.util.Vector;

public class ChatController {
    final static String STATUS = "/stat";
    final static String HELP_CMD = "/help";
    final static String SHOW_ROOMS_CMD = "/rooms";
    final static String JOIN_ROOM_CMD = "/join";
    final static String QUITE_CMD = "/quit";
    final static String LEAVE_ROOM_CMD = "/leave";
    final static String PRV_MSG_CMD = "/prv";
    final static String ANON_MSG_CMD = "/anon";
    final static String BLOCK = "/block";
    final static String UNBLOCK = "/unblock";
    final static String BLOCKED_LIST = "/blocklist";
    final static String MAKE_ROOM_CMD = "/newroom";
    final static String BAN_USER_CMD = "/ban";
    final static String UNBAN_USER_CMD = "/unban";
    final static String UNBANALL_CMD = "/unbanall";
    final static String SHOW_BANNED_CMD = "/banned";
    private Server server;
    private ClientView clientView;

    public ChatController(Server server, ClientView clientView) {
        this.server = server;
        this.clientView = clientView;
    }

    public void fireConnectionEvent(Client client) {
        server.closedConnectionPerformed(client);
    }

    public void fireMessageEvent(Client client, String message) {

        Vector<String> tokens = new Vector<>();

        StringTokenizer tokenizer = new StringTokenizer(message, " \t\r\n");

        while (tokenizer.hasMoreElements())
            tokens.add(tokenizer.nextToken());

        if (tokens.size() == 0)
            return;

        boolean correctCommand = false;

        switch (tokens.firstElement()) {
            case HELP_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    clientView.displayHelp(client);
                break;
            case SHOW_ROOMS_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    clientView.showRooms(client);
                break;
            case JOIN_ROOM_CMD:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.joinRoom(client, tokens.elementAt(1));
                break;
            case QUITE_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    server.unregisterClient(client);
                break;
            case LEAVE_ROOM_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    server.leaveRoom(client);
                break;
            case PRV_MSG_CMD:
                if (tokens.size() >= 3 && (correctCommand = true))
                    server.privateMessage(client, tokens.elementAt(1),
                            message.substring(message.indexOf(tokens.elementAt(1)) +
                                    tokens.elementAt(1).length()));
                else if (tokens.size() == 2)
                {
                    String status = tokens.elementAt(1);
                    if(status.equalsIgnoreCase("on") ||
                            status.equalsIgnoreCase("off")) {
                        correctCommand = true;
                        server.setPrivate(client, status);
                    }
                }
                break;
            case ANON_MSG_CMD:
                if (tokens.size() >= 3 && (correctCommand = true))
                    server.anonMessage(client, tokens.elementAt(1),
                            message.substring(message.indexOf(tokens.elementAt(1)) +
                                    tokens.elementAt(1).length()));
                else if (tokens.size() == 2)
                {
                    String status = tokens.elementAt(1);
                    if(status.equalsIgnoreCase("on") ||
                            status.equalsIgnoreCase("off")) {
                        correctCommand = true;
                        server.setAnon(client, status);
                    }
                }
                break;
            case MAKE_ROOM_CMD:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.makeRoom(client, tokens.elementAt(1));
                break;
            case BAN_USER_CMD:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.banUser(client, tokens.elementAt(1));
                break;
            case UNBAN_USER_CMD:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.unbanUser(client, tokens.elementAt(1));
                break;
            case UNBANALL_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    server.unbanAll(client);
                break;
            case SHOW_BANNED_CMD:
                if (tokens.size() == 1 && (correctCommand = true))
                    server.showBanned(client);
                break;
            case STATUS:
                if (tokens.size() == 1 && (correctCommand = true))
                    clientView.showStatus(client);
                break;
            case BLOCK:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.blockUser(client, tokens.elementAt(1));
                break;
            case UNBLOCK:
                if (tokens.size() == 2 && (correctCommand = true))
                    server.unblockUser(client, tokens.elementAt(1));
                break;
            case BLOCKED_LIST:
                if (tokens.size() == 1 && (correctCommand = true))
                    server.showBlocked(client);
                break;
            default:
                if((!tokens.firstElement().startsWith("/")) && (correctCommand = true))
                    server.publicMessage(client, message);
        }

        if (!correctCommand)
            clientView.sendErrorMessage(client, "Invalid command format: " + message);
    }
}