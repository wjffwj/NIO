package nine;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NIoClient {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8899));
        socketChannel.configureBlocking(true);
        String fileName = "D:\\ideacode1\\NIO\\src\\main\\java\\nine\\操作系统零拷贝.png";
        FileChannel fileChannel = new FileInputStream(fileName).getChannel();
        long startTIme = System.currentTimeMillis();
        long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);//零拷贝

    }
}
