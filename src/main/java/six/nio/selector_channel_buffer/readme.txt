传统网络编程
ServerSocket serverSocket =。。。。
serverSocket.bind(port);
Socket socket = serverSocket.accept();//阻塞方法

while(true){
    将socket放到线程池中 交给线程池中的任务处理
}
服务端会找一个操作系统中空闲的端口号返回给客户端 而不是只用一个端口号返回 （学到了）

-------------------------------------------------------------------------------
NIO可以解决阻塞式IO  不用一个socket对应一个线程   可以1个线程处理多个客户端
    Selector:
