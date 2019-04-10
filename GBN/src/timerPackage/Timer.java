package timerPackage;

import gbn.GBNClient;
import gbn.GBNServer;


/**
 * 计时器
 */
public class Timer extends Thread {

    private Model model;
    private GBNClient gbnClient;
    private GBNServer gbnServer;
    
    public Timer(GBNClient gbnClient, Model model){
        this.gbnClient = gbnClient;
        this.model = model;
    }
    public Timer(GBNServer gbnServer, Model model) {
    	this.gbnServer = gbnServer;
    	this.model = model;
    }
    @Override
    public void run(){
        do{
            int time = model.getTime();
            if(time > 0){
                try {
                    Thread.sleep(time*1000);

                    System.out.println("\n");
                   if(gbnClient == null) {
                	   System.out.println("GBN服务器端等待ACK超时");
                	   gbnServer.timeOut();
                   }else {
                	   System.out.println("GBN客户端等待ACK超时");
                       gbnClient.timeOut();
                   }
                    
                    
                    model.setTime(0);

                } catch (InterruptedException e) {
                } catch (Exception e) {
                }
            }
        }while (true);
    }
}

