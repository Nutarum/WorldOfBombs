package server;

import com.jmr.wrapper.common.exceptions.NNCantStartServer;
import com.jmr.wrapper.server.Server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

class UDPServer {
    int port = 1234;
    ServerJFrame serverJFrame;
    private Server server;
    ServerListener serverListener;
    public UDPServer(ServerJFrame serverJFrame) {
        this.serverJFrame = serverJFrame;
        try {
            server = new Server(port, port);
            server.start();
            serverListener = new ServerListener(serverJFrame);
            server.setListener(serverListener);
            if (server.isConnected()) {
                serverJFrame.consoleLog("Server has started.");
            }else{
                serverJFrame.consoleLog("Error starting.");
            }
        } catch (NNCantStartServer e) {
            serverJFrame.consoleLog("Exception: "+e);
        }  
        serverJFrame.consoleLog("Port: " + port);
        try {
                Enumeration e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements())
                {
                    NetworkInterface n = (NetworkInterface) e.nextElement();
                    Enumeration ee = n.getInetAddresses();
                    while (ee.hasMoreElements())
                    {
                        InetAddress i = (InetAddress) ee.nextElement();
                        if(i.getHostAddress().startsWith("192.")){
                              serverJFrame.consoleLog("Posible local IP: " + i.getHostAddress());
                        }                       
                    }
                }                
            InetAddress IP;              
            IP = InetAddress.getLocalHost();
              serverJFrame.consoleLog("Local IP: " + IP.getHostAddress());
        } catch (UnknownHostException ex) {       
               serverJFrame.consoleLog("UnknownHostException: " + ex.toString());
        } catch (SocketException ex) {
               serverJFrame.consoleLog("SocketException: " + ex.toString());
        }
        URL whatismyip;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine();
             serverJFrame.consoleLog("Internet IP: " + ip);
        } catch (Exception ex) {
             serverJFrame.consoleLog("Internet IP not found exception: "+ex.toString());
        }
    }
}
