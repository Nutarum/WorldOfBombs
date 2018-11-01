package client;

import World.Const;
import World.PlayerSend;
import World.World;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import packets.PlayerInput;

public class GamePanel extends JPanel {

    public World world = new World();
    public boolean w, a, s, d, spc;
    ClientManager clientManager;

    BufferedImage imgPlayer, imgBlock, imgBrick, imgBomb, imgUpgradeDeath, imgUpgradeBomb, imgUpgradeDamage, imgUpgradeSpeed;
    HashMap<String, BufferedImage> imgExplosion;

    public GamePanel(ClientManager clientManager) {
        this.clientManager = clientManager;

        imgPlayer = loadImage("/images/player.png");
        imgBlock = loadImage("/images/block.png");
        imgBrick = loadImage("/images/brick.png");
        imgBomb = loadImage("/images/bomb.png");
        imgUpgradeDeath = loadImage("/images/upgrade_death.png");
        imgUpgradeBomb = loadImage("/images/upgrade_bomb.png");
        imgUpgradeDamage = loadImage("/images/upgrade_damage.png");
        imgUpgradeSpeed = loadImage("/images/upgrade_speed.png");

        imgExplosion = new HashMap<String, BufferedImage>();
        imgExplosion.put("explosion", loadImage("/images/explosion.png"));
        imgExplosion.put("explosionwasd", loadImage("/images/explosionwasd.png"));
        imgExplosion.put("explosionws", loadImage("/images/explosionws.png"));
        imgExplosion.put("explosionad", loadImage("/images/explosionad.png"));
        imgExplosion.put("explosionw", loadImage("/images/explosionw.png"));
        imgExplosion.put("explosiona", loadImage("/images/explosiona.png"));
        imgExplosion.put("explosions", loadImage("/images/explosions.png"));
        imgExplosion.put("explosiond", loadImage("/images/explosiond.png"));
        imgExplosion.put("explosionwas", loadImage("/images/explosionwas.png"));
        imgExplosion.put("explosionasd", loadImage("/images/explosionasd.png"));
        imgExplosion.put("explosionwsd", loadImage("/images/explosionwsd.png"));
        imgExplosion.put("explosionwad", loadImage("/images/explosionwad.png"));
        imgExplosion.put("explosionwa", loadImage("/images/explosionwa.png"));
        imgExplosion.put("explosionas", loadImage("/images/explosionas.png"));
        imgExplosion.put("explosionsd", loadImage("/images/explosionsd.png"));
        imgExplosion.put("explosionwd", loadImage("/images/explosionwd.png"));
    }

    public BufferedImage loadImage(String str) {
        try {
            return ImageIO.read(this.getClass().getResource(str));
        } catch (IOException ex) {
            clientManager.clientJFrame.consoleLog("ERROR LOADING IMAGE: " + ex);
            Logger.getLogger(GamePanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(590, 510);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.getHSBColor(0.35f, 0.6f, 0.6f));
        g.fillRect(0, 0, 600, 520);

        for (int i = 0; i < Const.mapWidth; i++) {
            for (int j = 0; j < Const.mapHeight; j++) {
                if (world.map[i][j] == 0) {
                } else if (world.map[i][j] == 1) {
                    g.drawImage(imgBlock, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] == 2) {
                    g.drawImage(imgBrick, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] == 3) {//mejora daño
                    g.drawImage(imgUpgradeDamage, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] == 4) {//mejora speed
                    g.drawImage(imgUpgradeSpeed, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] == 5) {//mejora bomba
                    g.drawImage(imgUpgradeBomb, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] == 6) {//mejora muerte
                    g.drawImage(imgUpgradeDeath, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] >= -128 && world.map[i][j] < -67) {
                    g.drawImage(imgBomb, i * 40, j * 40, Const.tileSize, Const.tileSize, this);
                } else if (world.map[i][j] > -68 && world.map[i][j] < -24) {
                    String explos = "explosion";
                    if(j!=0 && (world.map[i][j-1] > -68 && world.map[i][j-1] < -24)){
                        explos+="w";
                    }
                    if(i!=0 && (world.map[i-1][j] > -68 && world.map[i-1][j] < -24)){
                        explos+="a";
                    }
                     if(j!=Const.mapHeight-1 && (world.map[i][j+1] > -68 && world.map[i][j+1] < -24)){
                       explos+="s";
                    } 
                    if(i!=Const.mapWidth-1 && (world.map[i+1][j] > -68 && world.map[i+1][j] < -24)){
                       explos+="d";
                    }
                                    
                    g.drawImage(imgExplosion.get(explos), i * 40, j * 40, Const.tileSize, Const.tileSize, this);                
                }
            }
        }
        for (int i = 0; i < world.players.size(); i++) {
            PlayerSend p = world.players.get(i);
            g.drawImage(imgPlayer, p.x, p.y, Const.playerWidth, Const.playerHeight, this);
        }

    }

    public void sendCommands() {
        PlayerInput pInput = new PlayerInput(w, a, s, d, spc);
        clientManager.sendMessage(pInput);
    }
}
