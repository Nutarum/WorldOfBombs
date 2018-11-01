
package server;

import server.game.Game;

class GameThread extends Thread{
    Game game;
    public GameThread(Game game){
        this.game = game;
    }
    public void run(){
      game.startRun();
    }
}
