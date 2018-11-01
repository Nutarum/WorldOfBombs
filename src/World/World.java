package World;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class World implements Serializable {
    public byte[][] map = new byte[15][13];//-128 to 127    
    public ArrayList<PlayerSend> players = new ArrayList<>();
    public World(){
        for(int i=0;i<15;i++){
             for(int j=0;j<13;j++){
                if(i%2!=0 && j%2!=0){
                    map[i][j]=1;
                }else{
                    map[i][j]=0;
                }
            }
        }
        int nBricks=80;
        Random r = new Random();
        for(int i=0;i<nBricks;i++){
            boolean ok = false;
            int x=0;
            int y=0;            
            while(!ok){
                x = r.nextInt(15);
                y = r.nextInt(13);                
                if(map[x][y]==0){
                    //No permitimos que las 4 esquinas empiecen con bloques
                    if(!((x<2 && (y<2||y>10) || (x>12 && (y<2||y>10))))){
                        map[x][y]=2;
                        ok=true;
                    }
                }
            }
        }
    }
}
