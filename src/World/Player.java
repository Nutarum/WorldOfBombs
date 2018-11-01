package World;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable{
   public boolean dead = false;
   public int x;
   public int y;
   public int speed = 4;
   public int strength = 3;
   public int bombLimit = 1;
   public int bombCooldown = 0;
   public ArrayList<Bomb> bombs = new ArrayList<>();
   public Player (int x, int y){
      this.x=x;
      this.y=y;
   }
}
