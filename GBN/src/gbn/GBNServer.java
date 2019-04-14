package gbn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import timerPackage.Model;
import timerPackage.Timer;
import tool.Crc;
import tool.ReadIniFile;

/**
 * 服务器端
 */
public class GBNServer extends Thread{
	private String path = "E:\\\\\\\\NetworkExperiment\\\\\\\\computer-network-lab-code\\\\\\\\chapter 3\\\\\\\\lab4\\\\\\\\java\\\\\\\\bin\\Server.ini";
    private  int portReceive = 8888 ;
    private  int portSend = 8800;
    private int FilterError ;
    private int FilterLost ;
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
    String[] dataSend = {"1000000000", "101", "1001001", "110011", "11000000100011","1110000111","11100100111", "11011000110011","1111000010001111","1010101010101"};
    
    public GBNServer(GBNServer a) {
    	this.gbnServer = a;
    }
    public GBNServer()  throws Exception {
    	Scanner sc = new Scanner(System.in);
    	
    	System.out.println("请输入服务器配置文件路径(使用默认配置请直接回车)：");
    	String p = sc.nextLine();
    	
    	if(!p.contentEquals("")) {
    		System.out.println("输入的文件路径为:" + p);
    		path = p;
    	}
    	ReadIniFile rf = new ReadIniFile(path);
    		
    	Map<String,String> map = ReadIniFile.readFile();
    	
    	portReceive = Integer.parseInt(map.get("portReceive"));
    	portSend = Integer.parseInt(map.get("portSend"));
    	FilterError = Integer.parseInt(map.get("FilterError"));
    	FilterLost = Integer.parseInt(map.get("FilterLost"));
    	
//    	System.out.println("portReceive " + portReceive);
//    	System.out.println("portSend " + portSend);
    	model = new Model();
        timer = new Timer(this,model);
        model.setTime(0);
        timer.start();
        gbnServer = new GBNServer(this);
        gbnServer.start();
        Thread.sleep(3000);
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
            System.out.println();
            System.out.println("--------------------接收-------------------\n"+"(Client ---> Server)收到确认帧：[ACK = " + ack+"]");
            //System.out.println("收到帧： [ACK](Client ---> Server) 从客户端获得的数据:" + fromServer );
            //System.out.println( "此时base为 " + base);
            System.out.println("\n");
        }
//           
        }
    
    @Override
	public void run() {
		// TODO Auto-generated method stub
    	//接收客户端数据
    	System.out.println("监听客户端线程启动\n");
    	 try {
             datagramSocket = new DatagramSocket(portReceive);
             while (true) {
                 byte[] receivedData = new byte[4096];
                 datagramPacket = new DatagramPacket(receivedData, receivedData.length);
                 datagramSocket.receive(datagramPacket);
                 //收到的数据
                 String received = new String(datagramPacket.getData()).trim();
                 String[] info = received.split("-");
                 //System.out.println(received);
                 System.out.println();
                 if(Integer.parseInt(info[0]) == exceptedSeq) {
                	 StringBuffer sb = new StringBuffer(info[1] + info[2]);
                	 
                	 
                     String check = Crc.crc_check(sb);
                     int pos = check.indexOf("1");
                     
                     if(pos == -1) {
                    	 //此时数据正确
                    	 System.out.println("--------------------接收-------------------\n"+"收到数据帧: [Num=" + info[0] +"][Data=" + info[1] + "]\n" + "Frame_expected = " + exceptedSeq + "\nCRC校验成功");
                    	 System.out.println();
                    	 
                    	 sendAck(exceptedSeq);
                    	 
                    	 //System.out.println("[Data](Client ---> Server) 客户端发送帧的序号为: " + info[0] + ",客户端发送的数据为 " + info[1] + "服务器期待的数据编号: " + exceptedSeq);
                    	 exceptedSeq++;
                    	 
                     }else {
                    	 //此时数据出错
                    	 System.out.println("--------------------接收-------------------\n"+"收到数据帧: [Num=" + info[0] +"][Data=" + info[1] + "]\n" +"Frame_expected = " + exceptedSeq + "\nCRC校验失败");
                    	 
                    	 sendAck(exceptedSeq -1);
                    	 System.out.println();
                     }
                 }else {
                	 System.out.println("--------------------接收-------------------\n"+"收到数据帧: [Num=" + info[0] +"][Data=" + info[1] + "]\n" + "Frame_expected = " + exceptedSeq + "\n丢弃非预期帧");
                	 
                     //仍发送之前的ack
                     sendAck(exceptedSeq - 1);
                     System.out.println();
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
        	System.out.println();
        	StringBuffer sb = new StringBuffer("" + i + dataSend[i-1]);
            String remainder = Crc.crc_remainder(new StringBuffer("" + dataSend[i-1]));
            String ServerData = "" + i + "-" + dataSend[i-1] + "-" + remainder;
            
            System.out.println("--------------------超时重发-------------------\n"+"(Server ---> Client) ACK_expected = " + i + ", Next_frame_to_send = " +( i+1) + ", Frame_expected = " + exceptedSeq + "\n" + "待发送帧：[" + i + "]  [Data=" + dataSend[i -1] + "]\n"+"模拟：重新发送");
        	
        	System.out.println();
            byte[] data = ServerData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, portSend);
            datagramSocket.send(datagramPacket);
        }
    }
    private void sendData() throws Exception {
    	datagramSocket = new DatagramSocket();
        inetAddress = InetAddress.getLocalHost();
        while (nextSeq < base + N && nextSeq <= dataSend.length) {
        	System.out.println();
            //不发编号为3的数据，模拟数据丢失
            if((nextSeq%10) == 3) {
            	System.out.println("--------------------发送-------------------\n"+"(Server ---> Client) ACK_expected = " + nextSeq + ", Next_frame_to_send = " +( nextSeq+1) + ", Frame_expected = " + exceptedSeq + "\n" + "待发送帧：[" + nextSeq + "]  [Data=" + dataSend[nextSeq -1] + "]\n" + "模拟：丢失");
            	
            	System.out.println();
                nextSeq++;
                continue;
            }

            StringBuffer sb = new StringBuffer("" + nextSeq + dataSend[nextSeq-1]);
            String remainder = Crc.crc_remainder(new StringBuffer("" + dataSend[nextSeq -1]));
            String ServerData ;
            if(nextSeq%10 == 9) {//发送错误帧
            	ServerData = "" + nextSeq + "-" + dataSend[nextSeq-1] + "1" + "-"+ remainder;
            	System.out.println("--------------------发送-------------------\n"+"(Server ---> Client) ACK_expected = " + nextSeq + ", Next_frame_to_send = " +( nextSeq+1) + ", Frame_expected = " + exceptedSeq + "\n" + "待发送帧：[" + nextSeq + "]  [Data=" + dataSend[nextSeq -1] + "]\n" + "模拟：传输错误");
            	
            	System.out.println();
            	
            }else {
            	ServerData = ""+nextSeq + "-" +  dataSend[nextSeq-1] +"-" + remainder;
            	System.out.println("--------------------发送-------------------\n"+"(Server ---> Client) ACK_expected = " + nextSeq + ", Next_frame_to_send = " +( nextSeq+1) + ", Frame_expected = " + exceptedSeq + "\n" + "待发送帧：[" + nextSeq + "]  [Data=" + dataSend[nextSeq -1] + "]\n" + "模拟：正确发送");
            	
            	System.out.println();
            }
            
            //System.out.println("(Server ---> Client) 向客户端发送帧的序号为: "+nextSeq+",要发送的数据为: " + dataSend[nextSeq-1]);
            
          
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
