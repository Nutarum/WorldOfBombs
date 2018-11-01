package client;

import com.jmr.wrapper.client.Client;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import packets.ChatMessage;

public class ClientManager {
        private int port = 1234;
        //private String ip = "85.219.45.217";
       // private String ip = "83.173.155.170";
        public String ip = "127.0.0.1";
	private Client client;
	public ClientJFrame clientJFrame;
	public ClientManager(ClientJFrame clientJFrame) {     
            this.clientJFrame=clientJFrame;
            File archivo = new File("config.txt");
            FileReader fr;
            try {
                fr = new FileReader(archivo);
                BufferedReader br = new BufferedReader(fr);
                ip = br.readLine();
            } catch (Exception ex) {
                 ip = "127.0.0.1";
            }
        
		client = new Client(ip, 1234, 1234);
		client.setListener(new ClientListener(clientJFrame));
		client.connect();
		
		if (client.isConnected()) {
                    clientJFrame.consoleLog("Connected");		
		}else{
                    clientJFrame.consoleLog("Error connecting");
                }
	}	

    void sendMessage(Object object) {
        if (object instanceof ChatMessage) {
           client.getServerConnection().sendUdp(object);
	}else{
           client.getServerConnection().sendUdp(object);
        }
    }
}
