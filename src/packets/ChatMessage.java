package packets;

import java.io.Serializable;

public class ChatMessage implements Serializable {	
	public String username,password, message;	
	public ChatMessage(String username,String password, String message) {
		this.username = username;
                this.password = password;
		this.message = message;
	}
}
