package World;

import java.io.Serializable;
import java.util.ArrayList;

public class Bomb implements Serializable{
    public ArrayList<Integer> insidePlayers = new ArrayList<>();
    public int x=0;
    public int y=0;
    public int time = 60;
    public Bomb(int x,int y){
        this.x=x;
        this.y=y;
    }
}
