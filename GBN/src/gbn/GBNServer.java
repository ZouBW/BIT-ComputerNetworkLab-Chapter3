package gbn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import timerPackage.Model;
import timerPackage.Timer;

/**
 * 服务器端
 */
public class GBNServer extends Thread{
    private final int portReceive = 80;
    private final int portSend = 8800;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private int exceptedSeq = 1;
    private static GBNServer gbnServer;
    //发送信息
    private static Model model;
    private Timer timer;
    private int nextSeq = 1;
    private int base = 1;
    private int N = 5;
    private InetAddress inetAddress;
    
    public GBNServer(GBNServer a) {
    	this.gbnServer = a;
    }
    public GBNServer()  throws Exception {
    	model = new Model();
        timer = new Timer(this,model);
        model.setTime(0);
        timer.start();
        gbnServer = new GBNServer(this);
        gbnServer.start();
        Thread.sleep(5000);
        while(true){
            //向客户端端发送数据
            sendData();
            //从客户端端接受ACK
            byte[] bytes = new byte[4096];
            datagramPacket = new DatagramPacket(bytes, bytes.length);
            datagramSocket.receive(datagramPacket);
            String fromServer = new String(bytes, 0, bytes.length);
            int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("ack:")+4).trim());
            base = ack+1;
            if(base == nextSeq){
                //停止计时器
                model.setTime(0);
            }else {
                //开始计时器
                model.setTime(3);
            }
            System.out.println("[ACK](Client ---> Server) 从客户端获得的数据:" + fromServer );
            //System.out.println( "此时base为 " + base);
            System.out.println("\n");
        }
//            try {
//                datagramSocket = new DatagramSocket(portReceive);
//                while (true) {
//                    byte[] receivedData = new byte[4096];
//                    datagramPacket = new DatagramPacket(receivedData, receivedData.length);
//                    datagramSocket.receive(datagramPacket);
//                    //建立发送线程
//                    if(chance > 0) {
//                    	chance -= 1;
//                    	gbnServer.start();
//                    }
//                    //收到的数据
//                    String received = new String(receivedData, 0, receivedData.length);//offset是初始偏移量
//                    System.out.println(received);
//                    //收到了预期的数据
//                    if (Integer.parseInt(received.substring(received.indexOf("编号:") + 3).trim()) == exceptedSeq) {
//                        //发送ack
//                        sendAck(exceptedSeq);
//                        System.out.println("服务端期待的数据编号:" + exceptedSeq);
//                        //期待值加1
//                        exceptedSeq++;
//                        System.out.println('\n');
//                    } else {
//                        System.out.println("服务端期待的数据编号:" + exceptedSeq);
//                        System.out.println("+++++++++++++++++++++服务器未收到预期数据+++++++++++++++++++++");
//                        //仍发送之前的ack
//                        sendAck(exceptedSeq - 1);
//                        System.out.println('\n');
//                    }
//                }
//            }catch(SocketException e){
//                    e.printStackTrace();
//                }
        }
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
    	//向客户端发送数据
    	 try {
             datagramSocket = new DatagramSocket(portReceive);
             while (true) {
                 byte[] receivedData = new byte[4096];
                 datagramPacket = new DatagramPacket(receivedData, receivedData.length);
                 datagramSocket.receive(datagramPacket);
                 //收到的数据
                 String received = new String(receivedData, 0, receivedData.length);//offset是初始偏移量
                 System.out.println(received);
                 //收到了预期的数据
                 if (Integer.parseInt(received.substring(received.indexOf("编号:") + 3).trim()) == exceptedSeq) {
                     //发送ack
                     sendAck(exceptedSeq);
                     System.out.println("服务器期待的数据编号:" + exceptedSeq);
                     //期待值加1
                     exceptedSeq++;
                     System.out.println('\n');
                 } else {
                     System.out.println("服务器期待的数据编号:" + exceptedSeq);
                     System.out.println("+++++++++++++++++++++服务器未收到预期数据+++++++++++++++++++++");
                     //仍发送之前的ack
                     sendAck(exceptedSeq - 1);
                     System.out.println('\n');
                 }
             }
         }catch(IOException e){
                 e.printStackTrace();
             }
	}

	public static final void main(String[] args) throws Exception {
        new GBNServer();
    }
    
    //向客户端发送ack
    public void sendAck(int ack) throws IOException {
        String response = " ack:"+ack;
        byte[] responseData = response.getBytes();
        InetAddress responseAddress = datagramPacket.getAddress();
        int responsePort = datagramPacket.getPort();
        datagramPacket = new DatagramPacket(responseData,responseData.length,responseAddress,responsePort);
        datagramSocket.send(datagramPacket);
    }
    public void timeOut() throws Exception {
        for(int i = base;i < nextSeq;i++){
            String clientData = "服务器重新发送的数据编号:" + i;
            System.out.println("(Client ---> Server) 向客户端重新发送的数据:" + i);
            byte[] data = clientData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, portSend);
            datagramSocket.send(datagramPacket);
        }
    }
    private void sendData() throws Exception {
    	datagramSocket = new DatagramSocket();
        inetAddress = InetAddress.getLocalHost();
        while (nextSeq < base + N && nextSeq <= 15) {
            //不发编号为3的数据，模拟数据丢失
            if(nextSeq == 3) {
                nextSeq++;
                continue;
            }

            String ServerData = "服务器发送的数据编号:" + nextSeq;
            System.out.println("(Server ---> Client) 向客户端发送的数据:"+nextSeq);

            byte[] data = ServerData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, portSend);
            datagramSocket.send(datagramPacket);

            if(nextSeq == base){
               //开始计时
                model.setTime(3);
            }
            nextSeq++;
        }
    }
}
