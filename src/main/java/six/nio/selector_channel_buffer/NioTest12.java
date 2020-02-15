package six.nio.selector_channel_buffer;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
服务器端监听5个端口号 用1个线程处理所有客户端连接
 */
public class NioTest12 {
    public static void main(String[] args) throws Exception {
        int ports[] = new int[5];
        ports[0] = 5000;
        ports[1] = 5001;
        ports[2] = 5002;
        ports[3] = 5003;
        ports[4] = 5004;

        Selector selector = Selector.open();

        for (int i = 0; i < ports.length; i++) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            ServerSocket socket = serverSocketChannel.socket();
            InetSocketAddress address = new InetSocketAddress(ports[i]);
            socket.bind(address);

            //t通道和选择器注册
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);//接受客户端连接
            System.out.println("监听端口：" + ports[i]);
        }

        while (true) {
            int numbers = selector.select();
            System.out.println("numbers" + numbers);

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            System.out.println("selectionKeys" + selectionKeys);
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    iterator.remove();
                    System.out.println("获得客户端连接： " + socketChannel);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    while (true) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                        byteBuffer.clear();
                        int read = socketChannel.read(byteBuffer);
                        if (read <= 0) {
                            break;
                        }
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                    }
                    iterator.remove();
                }

            }
        }

    }

}
