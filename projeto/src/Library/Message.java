package Library;

import static Server.myServer.usersP;

public class Message {

    private User sender;
    private User receiver;
    private String message;

    public Message(User sender, User receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public String getPath(){
        //../files/clientFiles/joao/
        return usersP + receiver.getId() + "messages.txt";
    }

    public User getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

}
