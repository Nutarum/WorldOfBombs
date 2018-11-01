package server;

import packets.ChatMessage;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;
import packets.PlayerInput;

public class ServerListener implements SocketListener {

    ServerJFrame serverJFrame;

    public ServerListener(ServerJFrame serverJFrame) {
        this.serverJFrame = serverJFrame;
        
        ConnectionManager.getInstance().setServerJFrame(serverJFrame);
    }

    @Override
    public void connected(Connection con) {
        serverJFrame.consoleLog("Client connected.");
        ConnectionManager.getInstance().addConnection(con);
    }

    @Override
    public void disconnected(Connection con) {
        serverJFrame.consoleLog("Client disconnected.");
        ConnectionManager.getInstance().removeConnection(con);
        for (Connection c : ConnectionManager.getInstance().connections) {
                c.sendUdp(new ChatMessage("SERVER", "", "Client has disconnected."));
            
        }
    }

    @Override
    public void received(Connection con, Object object) {
       if (object instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) object;
            serverJFrame.consoleLog(msg.username + ": " + msg.message);
            for (Connection c : ConnectionManager.getInstance().connections) {
                c.sendUdp(msg);                
            }
        }else if (object instanceof PlayerInput) {
            PlayerInput msg = (PlayerInput) object;
            ConnectionManager.getInstance().connectionsInfo.get(ConnectionManager.getInstance().connections.indexOf(con)).input = msg;
        }
    }

    void sendGlobalMessage(ChatMessage msg) {
        serverJFrame.consoleLog("SERVER: " + msg.message);
        for (Connection c : ConnectionManager.getInstance().connections) {
            c.sendUdp(msg);
        }
    }

}
