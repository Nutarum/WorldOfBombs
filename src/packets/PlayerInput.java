package packets;

import java.io.Serializable;


public class PlayerInput implements Serializable {
    public boolean w,a, s,d,spc;	
	public PlayerInput(boolean w,boolean a, boolean s, boolean d, boolean spc) {
		this.w = w;
                this.a = a;
		this.s = s;
                this.d = d;
                this.spc = spc;
	}
}
