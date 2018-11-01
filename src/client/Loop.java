package client;


public class Loop implements Runnable {

    public static GamePanel jpanel;

    public Loop(GamePanel jpanel) {
        this.jpanel = jpanel;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while (true) {
            long startTime = System.currentTimeMillis();
            jpanel.sendCommands();
            long tiempoBucle = System.currentTimeMillis() - startTime;              
            if (tiempoBucle < 30) {                
                try {
                    Thread.sleep(30 - tiempoBucle);                   
                } catch (InterruptedException ex) {
                }
            }else{
                jpanel.clientManager.clientJFrame.consoleLog("SLOW CLIENT LOOP");
            }
        }

    }

}
