package server;

import SharedUtils.Globals;
import java.util.ArrayList;

import com.jmr.wrapper.common.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import packets.ChatMessage;
import server.game.Game;

public class ConnectionManager {

    private static ConnectionManager instance = new ConnectionManager();

    public ArrayList<Connection> connections = new ArrayList<Connection>();
    public ArrayList<ConnectionInfo> connectionsInfo = new ArrayList<ConnectionInfo>();

    public ServerJFrame serverJFrame;

    public ArrayList<Game> games = new ArrayList<Game>();

    public void setServerJFrame(ServerJFrame serverJFrame) {
        this.serverJFrame = serverJFrame;
    }

    private ConnectionManager() {
    }

    public void addConnection(Connection con) {
        connections.add(con);
        connectionsInfo.add(new ConnectionInfo());
    }

    public void removeConnection(Connection con) {
        connectionsInfo.remove(connections.indexOf(con));
        connections.remove(con);
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    public void startGame() {
        int gameId = Globals.gameIdCount;
        Globals.gameIdCount++;
        serverJFrame.consoleLog("STARTING GAME " + gameId);

        for (Connection c : connections) {
            c.sendUdp(new ChatMessage("SERVER", "", "GAME START " + gameId));
        }
        //DA TIEMPO A LOS CLIENTES A QUE INICIEN SUS PARTIDAS  
        //evita un error de null pointer en el cliente al copiar el mundo 
        //enviado por el servidor, pero el cliente seria capaz de 
        //funcionar aun dando el error los primeros envios, asi que no preocuparse
        //de que el tiempo del sleep sea algo aleatorio
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        games.add(new Game(connections.size(), connections, gameId, serverJFrame));

        GameThread t = new GameThread(games.get(games.size() - 1));
        t.start();
    }
}
