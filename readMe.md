## 北理工计算机网络第三章实验 ##

#### 实验要求 ####
采用UDP Socket编程接口作为模拟物理层接口实现帧的发送和接收，协议采用双工方式进行数据通信。假设Host1和Host2分别向对方发送大文件，Host1先发送一帧到Host2，通过数据链路层的帧每次完成数据块的可靠传输，采用GBN协议，差错编码采用CRC-CCITT标准。以教材协议5为基础，在帧末尾增加CRC校验字段。

#### 运行环境 ####
* JavaSE 1.8
* Eclipse 19.03
* Windows 10

#### 运行方法 ####
将程序添加到Eclipse中运行，或运行<code>bin/gbn/</code>下class程序即可
