package gbn;

import timerPackage.Model;
import timerPackage.Timer;
import tool.Crc;
import tool.ReadIniFile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Scanner;

/**
 * 客户端
 */
public class GBNClient extends Thread{
	private String path = "E:\\\\NetworkExperiment\\\\computer-network-lab-code\\\\chapter 3\\\\lab4\\\\java\\\\bin\\Client.ini";
    private int portSend = 8888;
    private int portReceive = 8800;
    private int FilterError = 10;
    private int FilterLost = 10;
    private DatagramSocket datagramSocket ;
    private DatagramPacket datagramPacket;
    private InetAddress inetAddress;
    private Model model;
    private static GBNClient gbnClient;
    private  Timer timer;
    private int nextSeq = 1;
    private int base = 1;
    private int N = 5;
    
    //要发送的数据，自己按自己喜欢的来
    String[] dataSend = {
    		"000000000000000000", "00000010100000", "01000010000010", "0000000000011110000", "00001010101010000", "0000000011100001111000", "01000111000111000",
    		"0110001100011001100", "00001111111111111111111110000","0101"};
    

    //ACK数据
    private int exceptedSeq = 1;
    public GBNClient(GBNClient a) {
    	this.gbnClient = a;
    }
    public GBNClient() throws Exception 
    {
    	Scanner sc = new Scanner(System.in);
    	System.out.println("请输入客户端配置文件路径((使用默认配置请直接 回车)):");
    	
    	String p = sc.nextLine();
    	if(!p.equals("")) {
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
        gbnClient = new GBNClient(this);
        gbnClient.start();
       // Thread.sleep(6000);
        while(true){
            //向服务器端发送数据
            sendData();
            //从服务器端接受ACK
            byte[] bytes = new byte[4096];
            datagramPacket = new DatagramPacket(bytes, bytes.length);
            datagramSocket.receive(datagramPacket);
            String fromServer = new String(bytes, 0, bytes.length);
            //设立帧格式，第一个字符为0，则为ACK包,为1则为数据包
            int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("ack:")+4).trim());
            base = ack+1;
            if(base == nextSeq){
                //停止计时器
                model.setTime(0);
            }else {
                //开始计时器
                model.setTime(3);
            }
            System.out.println("[ACK](Server ---> Client) 从服务器获得的数据:" + fromServer );
            //System.out.println( "此时base为 " + base);
            System.out.println("\n");
        }

    }
    
    @Override
	public void run() {
    	//接收数据8800
		// TODO Auto-generated method stub
    	System.out.println("监听服务器线程启动\n");
    	 try {
             datagramSocket = new DatagramSocket(portReceive);
             while (true) {
                 byte[] receivedData = new byte[4096];
                 datagramPacket = new DatagramPacket(receivedData, receivedData.length);
                 datagramSocket.receive(datagramPacket);
                 //收到的数据
                 //String received = new String(receivedData, 0, receivedData.length);//offset是初始偏移量
                 String received = new String(datagramPacket.getData()).trim();
                 String[] info = received.split("-");
                
                 System.out.println(received);
                 //收到了预期的数据
                 if(Integer.parseInt(info[0]) == exceptedSeq) {
                	 StringBuffer sb ;
                	 
                		 sb= new StringBuffer(info[1] +info[2]);
                	 
                     String check = Crc.crc_check(sb);
                     //System.out.println("check ==" + check);
                     int pos = check.indexOf("1");
                     //System.out.println("pos ==" + pos);
                     if(pos == -1) {
                    	 //此时数据正确
                    	 sendAck(exceptedSeq);
                    	 System.out.println("----------------CRC校验通----------------");
                    	 
                    	 System.out.println("[Data](Server ----> Client) 服务器发送帧的序号为: " + info[0] + ",服务器发送的数据为 " + info[1] + "客户端期待的数据编号: " + exceptedSeq);
                    	 exceptedSeq++;
                    	 System.out.println();
                     }else {
                    	 //此时数据出错
                    	 System.out.println("-----------------------CRC校验不成功------------------------");
                    	 sendAck(exceptedSeq -1);
                    	 System.out.println();
                     }
                 }else {//不是期待数据
                	 System.out.println("客户端期待的数据编号:" + exceptedSeq);
                     System.out.println("+++++++++++++++++++++客户端未收到预期数据+++++++++++++++++++++");
                     //仍发送之前的ack
                     sendAck(exceptedSeq - 1);
                     System.out.println();
                 }
                 
//                 if (Integer.parseInt().trim()) == exceptedSeq) {
//                     //发送ack
//                     sendAck(exceptedSeq);
//                     System.out.println("客户端期待的数据编号:" + exceptedSeq);
//                     //期待值加1
//                     exceptedSeq++;
//                     System.out.println('\n');
//                 } else {
//                     System.out.println("客户端期待的数据编号:" + exceptedSeq);
//                     System.out.println("+++++++++++++++++++++客户端未收到预期数据+++++++++++++++++++++");
//                     //仍发送之前的ack
//                     sendAck(exceptedSeq - 1);
//                     //System.out.println('\n');
//                 }
             }
         }catch(IOException e){
                 e.printStackTrace();
             }
	}

	public static void main(String[] args) throws Exception {
         gbnClient = new GBNClient();

    }

    /**
     * 向服务器发送数据
     *
     * @throws Exception
     * 
     */
   
    public void sendAck(int ack) throws IOException {
        String response = " ack:"+ack;
        byte[] responseData = response.getBytes();
        InetAddress responseAddress = datagramPacket.getAddress();
        int responsePort = datagramPacket.getPort();
        datagramPacket = new DatagramPacket(responseData,responseData.length,responseAddress,responsePort);
        datagramSocket.send(datagramPacket);
    }
    /**
     * 超时数据重传
     */
    public void timeOut() throws Exception {
        for(int i = base;i < nextSeq;i++){
        	 StringBuffer sb = new StringBuffer("" + i + dataSend[i-1]);
             String remainder = Crc.crc_remainder(new StringBuffer("" + dataSend[i-1]));
            String clientData = "" + i + "-" + dataSend[i-1] + "-" + remainder ;
            System.out.println("(Client ---> Server) 向服务器重新发送帧的序号: " + i + ",要发送的数据为: " + dataSend[i-1]);
            byte[] data = clientData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, inetAddress, portSend);
            datagramSocket.send(datagramPacket);
        }
    }
    private void sendData() throws Exception {
    	datagramSocket = new DatagramSocket();
        inetAddress = InetAddress.getLocalHost();
        while (nextSeq < base + N && nextSeq <= dataSend.length) {
            //不发编号为3的数据，模拟数据丢失
            if((nextSeq%10) == 3) {
                nextSeq++;
                continue;
            }
            
            StringBuffer sb = new StringBuffer("" + nextSeq + dataSend[nextSeq-1]);
            String remainder = Crc.crc_remainder(new StringBuffer("" + dataSend[nextSeq -1]));
            String clientData;
            if(nextSeq % 10 == 9) {//发送错误数据帧
            	clientData = ""+nextSeq + "-"+ dataSend[nextSeq-1] + "1" +"-" + remainder;
            }else {
                clientData = ""+nextSeq + "-"+ dataSend[nextSeq-1] +"-" + remainder;

            }
            System.out.println("(Client ---> Server) 向服务器发送帧的序号为: "+nextSeq+",要发送的数据为: " + dataSend[nextSeq-1]);

            byte[] data = clientData.getBytes();
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
