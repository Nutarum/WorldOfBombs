package server.game;

import World.Const;
import World.Bomb;
import World.Player;
import World.PlayerSend;
import World.World;
import com.jmr.wrapper.common.Connection;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import packets.PlayerInput;
import server.ConnectionManager;
import server.ServerJFrame;

public class Game implements Runnable {

    boolean reseting=false;
    public World world;
    public int gameid;
    int gameType;
    ArrayList<Connection> cons;
    ArrayList<Player> players;
    ServerJFrame serverJFrame;  
    
    public Game(int i, ArrayList<Connection> cons, int id,ServerJFrame serverJFrame) {
        this.serverJFrame = serverJFrame;
        gameType = i;
        this.cons = cons;
        this.gameid = id;
        resetGame();
    }
   public void startRun(){
        run();
    }

    @Override
    public void run() {
        while (true) {
            reseting=false;
            long loopTime = System.currentTimeMillis();

            for (Connection con : cons) {
                int ind = ConnectionManager.getInstance().connections.indexOf(con);
                Player p = players.get(ind);
                if(!p.dead){
                    PlayerInput input = ConnectionManager.getInstance().connectionsInfo.get(ind).input;
                    if (input.w) {
                        movePlayer(ind, 0);
                    }
                    if (input.a) {
                        movePlayer(ind, 1);
                    }
                    if (input.s) {
                        movePlayer(ind, 2);
                    }
                    if (input.d) {
                        movePlayer(ind, 3);
                    }
                    if(p.bombCooldown>0){
                         p.bombCooldown--;                     
                    }
                    if (input.spc) {
                        if (p.bombCooldown==0 && p.bombs.size() < p.bombLimit && getPlayerTileType(p) == 0) {
                            world.map[getPlayerTileX(p)][getPlayerTileY(p)] = -128;
                            Bomb newBomb = new Bomb(getPlayerTileX(p), getPlayerTileY(p));
                            p.bombs.add(newBomb);
                            p.bombCooldown=6;
                            for(int i=0;i<players.size();i++){
                                Player pTemp = players.get(i);
                                if(colisionRect(pTemp.x, pTemp.y, Const.playerWidth, Const.playerHeight, newBomb.x*Const.tileSize, newBomb.y*Const.tileSize, Const.tileSize, Const.tileSize)){
                                    newBomb.insidePlayers.add(i);
                                }
                            }
                        }
                    }
                }                
            }
            //bucle para que las bombas comprueben si algun jugador a salido de ellas
            for(int i=0;i<players.size();i++){
                for(int j=0;j<players.get(i).bombs.size();j++){
                     Bomb b = players.get(i).bombs.get(j);
                    for(int k=players.get(i).bombs.get(j).insidePlayers.size()-1;k>-1;k--){
                        Player pTemp = players.get(players.get(i).bombs.get(j).insidePlayers.get(k));                       
                        if(!colisionRect(pTemp.x, pTemp.y, Const.playerWidth, Const.playerHeight, b.x*Const.tileSize, b.y*Const.tileSize, Const.tileSize, Const.tileSize)){
                            b.insidePlayers.remove(k);
                        }
                    }
                }
            }
            //Bucle para hacer pasar el tiempo en el mapa
            for (int i = 0; i < Const.mapWidth; i++) {
                for (int j = 0; j < Const.mapHeight; j++) {
                        if (world.map[i][j] >= -128 && world.map[i][j] < -68) {
                            world.map[i][j]++;
                        } else if (world.map[i][j] == -67) {
                            world.map[i][j] += 2;
                        } else if (world.map[i][j] > -67 && world.map[i][j] < -46) {
                            world.map[i][j]++;
                        } else if (world.map[i][j] == -46) {
                            world.map[i][j] = 0;
                        } else if (world.map[i][j] > -46 && world.map[i][j] < -25) {//explosion
                            world.map[i][j]++;
                        } else if (world.map[i][j] == -25) {//explosion
                            world.map[i][j] = (byte)randomizeMejora();
                        }
                    
                }
            }
            //bucle para detonaciones
            for (int i = 0; i < Const.mapWidth; i++) {
                for (int j = 0; j < Const.mapHeight; j++) {
                           if (world.map[i][j] == -68) {
                                detonate(i, j);
                            }   
                }
            }
            
            
            //MANDAR MUNDO
            //actualizar los players a mandar por los players del server
            for(int i=0;i<players.size();i++){
                world.players.get(i).x = players.get(i).x;                
                world.players.get(i).y = players.get(i).y;
            }
            //mandar mundo
            for (Connection c : cons) {
                c.sendUdp(world);
            }

            try {
                long sleepTime = 30 - (System.currentTimeMillis() - loopTime);
                if (sleepTime < 0) {
                    sleepTime = 0;
                    serverJFrame.consoleLog("SLOW SERVER LOOP");
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //dir: 0-w 1-a 2-s 3-d
    private void movePlayer(int ind, int dir) {
        Player p = players.get(ind);
        int pxTile = p.x / 40;
        int pyTile = p.y / 40;
        int px = p.x;
        int py = p.y;
        if (dir == 0) {
            p.y -= p.speed;
            if (pyTile == 0) {
                if (p.y < 0) {
                    p.y = 0;
                }
            } else {
                //Si ha cambiado de casilla
                if (p.y < (py / Const.tileSize) * Const.tileSize) {
                    //Comprueba su columna      
                    boolean matarJugador = false;
                    if (world.map[pxTile][pyTile - 1] > 2 && world.map[pxTile][pyTile - 1] < 31) {//mejora
                        cogerMejora(p, pxTile, pyTile - 1);
                    }else if(world.map[pxTile][pyTile - 1] < -67){ //bomba
                     comprobarColisionBomba(p,pxTile,pyTile-1,dir);
                    }else if (world.map[pxTile][pyTile - 1] > 0) { //bloque
                        p.y = (py / Const.tileSize) * Const.tileSize;
                    } else if (world.map[pxTile][pyTile - 1] < -24) { //explosion
                        matarJugador = true;
                    }
                    //Comprueba columna derecha si tiene que hacerlo
                    if ((pxTile != Const.mapWidth - 1) && ((p.x % Const.tileSize) > Const.tileSize - Const.playerWidth)) {
                        if (world.map[pxTile + 1][pyTile - 1] > 2 && world.map[pxTile + 1][pyTile - 1] < 31) {//mejora
                            cogerMejora(p, pxTile, pyTile - 1);
                        }else if(world.map[pxTile+1][pyTile - 1] < -67){ //bomba
                    comprobarColisionBomba(p,pxTile+1,pyTile-1,dir);
                    } else if (world.map[pxTile + 1][pyTile - 1] > 0) { //bloque
                            p.y = (py / Const.tileSize) * Const.tileSize;
                            matarJugador = false;
                        } else if (world.map[pxTile + 1][pyTile - 1] < -24) { //explosion
                            matarJugador = true;
                        }
                    }
                    if (matarJugador) {
                        matarJugador(p);
                    }
                }
            }
        } else if (dir == 1) {
            p.x -= p.speed;
            if (pxTile == 0) {
                if (p.x < 0) {
                    p.x = 0;
                }
            } else {
                //Si ha cambiado de casilla
                if (p.x < (px / Const.tileSize) * Const.tileSize) {
                    //Comprueba su columna      
                    boolean matarJugador = false;
                    if (world.map[pxTile - 1][pyTile] > 2  && world.map[pxTile - 1][pyTile] < 31) {//mejora
                        cogerMejora(p, pxTile - 1, pyTile);
                    }else if(world.map[pxTile-1][pyTile ] < -67){ //bomba
                    comprobarColisionBomba(p,pxTile-1,pyTile,dir);
                    } else if (world.map[pxTile - 1][pyTile] > 0) { //bloque
                        p.x = (px / Const.tileSize) * Const.tileSize;
                    } else if (world.map[pxTile - 1][pyTile] < -24) { //explosion
                        matarJugador = true;
                    }
                    //Comprueba columna derecha si tiene que hacerlo
                    if ((pyTile != Const.mapHeight - 1) && ((p.y % Const.tileSize) > Const.tileSize - Const.playerHeight)) {
                        if (world.map[pxTile - 1][pyTile + 1] > 2 && world.map[pxTile - 1][pyTile + 1] < 31) {//mejora
                            cogerMejora(p, pxTile - 1, pyTile + 1);
                        }else if(world.map[pxTile-1][pyTile + 1] < -67){ //bomba
                     comprobarColisionBomba(p,pxTile-1,pyTile+1,dir);
                    } else if (world.map[pxTile - 1][pyTile + 1] > 0) { //bloque
                            p.x = (px / Const.tileSize) * Const.tileSize;
                            matarJugador = false;
                        } else if (world.map[pxTile - 1][pyTile + 1] < -24) { //explosion
                            matarJugador = true;
                        }
                    }
                    if (matarJugador) {
                        matarJugador(p);
                    }
                }
            }
        } else if (dir == 2) {
            p.y += p.speed;
            if (pyTile == Const.mapHeight - 1) {
                if (p.y > (Const.mapHeight) * 40 - Const.playerHeight) {
                    p.y = (Const.mapHeight) * 40 - Const.playerHeight;
                }
            } else {
                //Si ha cambiado de casilla
                if (p.y > (((py / Const.tileSize) * Const.tileSize) + Const.tileSize - Const.playerWidth)) {
                    //Comprueba su columna      
                    boolean matarJugador = false;
                    if (world.map[pxTile][pyTile + 1] > 2 && world.map[pxTile][pyTile + 1] < 31) {
                        cogerMejora(p, pxTile, pyTile + 1);
                    }else if(world.map[pxTile][pyTile + 1] < -67){ //bomba
                    comprobarColisionBomba(p,pxTile,pyTile+1,dir);
                    } else if (world.map[pxTile][pyTile + 1] > 0) { // bloque
                        p.y = ((py / Const.tileSize) * Const.tileSize) + Const.tileSize - Const.playerWidth;
                    } else if (world.map[pxTile][pyTile + 1] < -24) { //explosion
                        matarJugador = true;
                    }
                    //Comprueba fila abajo si tiene que hacerlo
                    if ((pxTile != Const.mapWidth - 1) && ((p.x % Const.tileSize) > Const.tileSize - Const.playerWidth)) {
                        if (world.map[pxTile + 1][pyTile + 1] > 2 && world.map[pxTile + 1][pyTile + 1] < 31) {
                            cogerMejora(p, pxTile+1, pyTile + 1);
                        }else if(world.map[pxTile+1][pyTile + 1] < -67){ //bomba
                    comprobarColisionBomba(p,pxTile+1,pyTile+1,dir);
                    } else if (world.map[pxTile + 1][pyTile + 1] > 0) { //bloque
                            p.y = ((py / Const.tileSize) * Const.tileSize) + Const.tileSize - Const.playerWidth;
                            matarJugador = false;
                        } else if (world.map[pxTile + 1][pyTile + 1] < -24) { //explosion
                            matarJugador = true;
                        }
                    }
                    if (matarJugador) {
                        matarJugador(p);
                    }
                }
            }
        } else if (dir == 3) {
            p.x += p.speed;
            if (pxTile == Const.mapWidth - 1) {
                if (p.x > (Const.mapWidth) * 40 - Const.playerWidth) {
                    p.x = (Const.mapWidth) * 40 - Const.playerWidth;
                }
            } else {
                //Si ha cambiado de casilla
                if (p.x > (px / Const.tileSize) * Const.tileSize + Const.tileSize - Const.playerWidth) {
                    //Comprueba su columna      
                    boolean matarJugador = false;
                    if (world.map[pxTile + 1][pyTile] > 2 && world.map[pxTile + 1][pyTile] < 31) {
                        cogerMejora(p, pxTile + 1, pyTile);
                    }else if(world.map[pxTile+1][pyTile] < -67){ //bomba
                    comprobarColisionBomba(p,pxTile+1,pyTile,dir);
                    } else if (world.map[pxTile + 1][pyTile] > 0) { // bloque
                        p.x = (px / Const.tileSize) * Const.tileSize + Const.tileSize - Const.playerWidth;
                    } else if (world.map[pxTile + 1][pyTile] < -24) { //explosion
                        matarJugador = true;
                    }
                    //Comprueba fila abajo si tiene que hacerlo
                    if ((pyTile != Const.mapHeight - 1) && ((p.y % Const.tileSize) > Const.tileSize - Const.playerHeight)) {
                        if (world.map[pxTile + 1][pyTile + 1] > 2 && world.map[pxTile + 1][pyTile + 1] < 31) {
                            cogerMejora(p, pxTile + 1, pyTile + 1);
                        }else if(world.map[pxTile+1][pyTile +1] < -67){ //bomba
                        comprobarColisionBomba(p,pxTile+1,pyTile+1,dir);
                    } else if (world.map[pxTile + 1][pyTile + 1] > 0) { //bloque
                            p.x = (px / Const.tileSize) * Const.tileSize + Const.tileSize - Const.playerWidth;
                            matarJugador = false;
                        } else if (world.map[pxTile + 1][pyTile + 1] < -24) { //explosion
                            matarJugador = true;
                        }
                    }
                    if (matarJugador) {
                        matarJugador(p);
                    }
                }
            }
        }
        //COLISION CON OTROS PLAYERS
        for (int i = 0; i < players.size(); i++) {
            Player p2 = players.get(i);
            if (!p.equals(p2)) {
                if (colisionRect(p.x, p.y, Const.playerHeight, Const.playerWidth, p2.x, p2.y, Const.playerHeight, Const.playerWidth)){
                    
                
                //if (p.x + Const.playerWidth > p2.x && p.y + Const.playerHeight > p2.y && p.x < p2.x + Const.playerWidth && p.y < p2.y + Const.playerHeight) {
                    switch (dir) {
                        case 0:
                            p.y=p2.y+Const.playerHeight;
                            break;
                        case 1:
                             p.x=p2.x+Const.playerWidth;
                            break;
                        case 2:
                            p.y=p2.y-Const.playerHeight;
                            break;
                        case 3:
                             p.x=p2.x-Const.playerWidth;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void detonate(int x, int y) {
      
        //como la propia casilla no hace detonateTile, meto el codigo de matar jugadores aqui
         int realX = x*Const.tileSize;
        int realY = y*Const.tileSize;
      
        for (Player pl : players) {
            if(colisionRect(realX, realY, Const.tileSize, Const.tileSize, pl.x, pl.y, Const.playerHeight, Const.playerWidth)){
                matarJugador(pl);
            }
        }
        if(reseting){
            return;
        }
        
        //y continuamos con denotane
        int strength = 0;
        for (Player pl : players) {
            for (int i = pl.bombs.size() - 1; i >= 0; i--) {
                if (pl.bombs.get(i).x == x && pl.bombs.get(i).y == y) {
                    strength = pl.strength;
                    pl.bombs.remove(pl.bombs.get(i));
                    break;
                }
            }
        }
        world.map[x][y] = -67;
        //Checking up
        boolean tempBool = true;
        int tempStr = strength;
        int tempX = x;
        while (tempBool && tempStr > 0) {
            tempX--;
            if (tempX < 0) {
                tempBool = false;
            } else {
                tempStr--;
                tempBool = detonateTile(tempX, y);
            }
        }
        //Checking down
        tempBool = true;
        tempStr = strength;
        tempX = x;
        while (tempBool && tempStr > 0) {
            tempX++;
            if (tempX >= Const.mapWidth) {
                tempBool = false;
            } else {
                tempStr--;
                tempBool = detonateTile(tempX, y);
            }

        }
        //Checking left
        tempBool = true;
        tempStr = strength;
        int tempY = y;
        while (tempBool && tempStr > 0) {
            tempY--;
            if (tempY < 0) {
                tempBool = false;
            } else {
                tempStr--;
                tempBool = detonateTile(x, tempY);
            }
        }
        //Checking right
        tempBool = true;
        tempStr = strength;
        tempY = y;
        while (tempBool && tempStr > 0) {
            tempY++;
            if (tempY >= Const.mapHeight) {
                tempBool = false;
            } else {
                tempStr--;                                
                tempBool = detonateTile(x, tempY);               
            }

        }
    }

    private int getPlayerTileType(Player p) {
        return world.map[getPlayerTileX(p)][getPlayerTileY(p)];
    }

    private int getPlayerTileX(Player p) {
        return (p.x + Const.tileSize / 2) / Const.tileSize;
    }

    private int getPlayerTileY(Player p) {
        return (p.y + Const.tileSize / 2) / Const.tileSize;
    }

    private boolean detonateTile(int tempX, int y) {        
        int realX = tempX*Const.tileSize;
        int realY = y*Const.tileSize;
      
        for (Player pl : players) {
            if(colisionRect(realX, realY, Const.tileSize, Const.tileSize, pl.x, pl.y, Const.playerHeight, Const.playerWidth)){
                matarJugador(pl);
            }
        }
        if(reseting){  
            return false;
        }
        if (world.map[tempX][y] == 1) {//block
            return false;
        } else if (world.map[tempX][y] == 2) {//brick     
            Random r = new Random();
            if (r.nextInt(100) < 50) {
                world.map[tempX][y] = -66;
            } else {
                world.map[tempX][y] = -45;
            }
            return false;
        }
        if (world.map[tempX][y] > 2  && world.map[tempX][y] < 31) {//mejora
            world.map[tempX][y] = -66;
            return false;
        } else if (world.map[tempX][y] >= -128 && world.map[tempX][y] < -67) {//bomba                
            detonate(tempX, y);
            return false;
        } else if (world.map[tempX][y] == -67) {//bomba ya explotada este turno
            return false;
        } else if (world.map[tempX][y] > -67 && world.map[tempX][y] < -45) {//explosion
            world.map[tempX][y] = -66;
        } else if (world.map[tempX][y] > -46 && world.map[tempX][y] < -24) {//explosion
            world.map[tempX][y] = -45;
        } else {//vacio
            world.map[tempX][y] = -66;
        }
        return true;
    }

    private void cogerMejora(Player p, int x, int y) {
       switch (world.map[x][y]){
           case 3:
               p.strength++;
               world.map[x][y]=0;
               break;
           case 4:
               p.speed++;
               world.map[x][y]=0;
               break;
           case 5:
               p.bombLimit++;
               world.map[x][y]=0;
               break;
           case 6:
               matarJugador(p);
               world.map[x][y]=0;
               break;
           default:
               break;
       }
    }

    private void matarJugador(Player p) {
        p.dead=true;
        p.x=Const.tileSize*20;
        p.y=Const.tileSize*20;
        int cont =0;
        for(int i=0;i<players.size();i++){
            if(!players.get(i).dead){
                cont++;
            }
        }
        if(cont <2){
            resetGame();
        }
    }

    private void resetGame() {
        reseting=true;
        world = new World();
        players = new ArrayList<>();
        int j = 0;
        for (Connection c : cons) {
            world.players.add(new PlayerSend());
            switch (j) {
                case 0:
                    players.add(new Player(5, 5));                  
                    break;
                case 1:
                   players.add(new Player(5 + Const.tileSize * (Const.mapWidth - 1), 5 + Const.tileSize * (Const.mapHeight - 1)));
                    break;
                case 2:
                    players.add(new Player(5 + Const.tileSize * (Const.mapWidth - 1), 5));
                    break;
                case 3:
                    players.add(new Player(5, 5 + Const.tileSize * (Const.mapHeight - 1)));
                    break;
                default:
                    players.add(new Player(5, 5));
                    break;
            }
            j++;
        }    
    }

    private int randomizeMejora() {  
        Random r = new Random();
        //return r.nextInt(28)+3; //ESTO SERIA PARA NUM DE 3 A 30
        return r.nextInt(4)+3;
    }
    
    private boolean colisionRect(int x1, int y1, int w1, int h1, int x2, int y2, int w2,int h2){
        if(x1 + w1 > x2 && y1 + h1 > y2 && x1 < x2 + w2 && y1 < y2 + h2){
            return true;
        }
        return false;
    }

    private void comprobarColisionBomba(Player p, int bombX, int bombY, int dir) {
       Bomb b = null;       
       for(int i=0;i<players.size();i++){
           Player tmp = players.get(i);
           for(int j=0;j<tmp.bombs.size();j++){
               b = players.get(i).bombs.get(j);
               if(b.x==bombX && b.y==bombY){                     
                   j=players.get(i).bombs.size();
                   i=players.size();
               }
           }
       }
       boolean puedoMoverme=false;
       for(int i=0;i<b.insidePlayers.size();i++){
           if(b.insidePlayers.get(i)==players.indexOf(p)){
               puedoMoverme=true;
           }
       }
       if(!puedoMoverme){
           switch(dir){
               case 0:
                    p.y= (bombY*Const.tileSize)+Const.tileSize;
                    break;
               case 1:
                    p.x= (bombX*Const.tileSize)+Const.tileSize;
                    break;
               case 2: 
                      p.y = (bombY*Const.tileSize)-Const.playerHeight;
                    break;
               case 3:
                    p.x = (bombX*Const.tileSize)-Const.playerWidth;
                    break;
               default:
                    break;                   
           }
       }
    }
}
