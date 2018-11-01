package client;

import World.World;
import packets.ChatMessage;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_SPACE;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ClientListener implements SocketListener {
         GamePanel gamePanel;
        ClientJFrame clientJFrame;
        public ClientListener(ClientJFrame clientJFrame){
            this.clientJFrame = clientJFrame;
        }
    
	@Override
	public void connected(Connection con) {
	}

	@Override
	public void disconnected(Connection con) {
	}

	@Override
	public void received(Connection con, Object object) {                     
		if (object instanceof ChatMessage) {
                   
                    ChatMessage msg = (ChatMessage) object;  
                    if (msg.username.equals("SERVER") && msg.message.startsWith("GAME START 0")){                         
                        clientJFrame.consoleLog("Starting game...");
                         EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("The Game");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                gamePanel = new GamePanel(clientJFrame.clientManager);
                frame.add(gamePanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setResizable(false);

                Loop loop = new Loop(gamePanel);

                   frame.addKeyListener(new KeyAdapter() {

                    public void keyPressed(KeyEvent e) {
                        switch(e.getKeyChar()){
                            case 'w':
                                gamePanel.w=true;
                                break;
                            case 'a':
                                gamePanel.a=true;
                                break;
                            case 's':
                                gamePanel.s=true;
                                break;
                            case 'd':
                                gamePanel.d=true;
                                break;
                             case VK_SPACE:
                                gamePanel.spc=true;
                                break;
                        }                      
                    }

                    public void keyReleased(KeyEvent e) {
                        switch(e.getKeyChar()){
                            case 'w':
                                gamePanel.w=false;
                                break;
                            case 'a':
                                gamePanel.a=false;
                                break;
                            case 's':
                                gamePanel.s=false;
                                break;
                            case 'd':
                                gamePanel.d=false;
                                break;
                             case VK_SPACE:
                                gamePanel.spc=false;
                                break;
                      }   
                    }
                });
            }
        });
                    }else{
                         clientJFrame.consoleLog("MSG>>" + msg.username + ": " + msg.message);
                    }                   
		}else if (object instanceof World) {
                     World msg = (World) object; 
                     
                     gamePanel.world = msg;
                  
                     gamePanel.repaint();
                }else{
                     clientJFrame.consoleLog("ELSE>>> " + object);     
                }
	}
}
