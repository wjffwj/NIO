package seven;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;

public class NioServer {
    public static Map<String, SocketChannel> map = new HashMap<String, SocketChannel>();

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        //通过serversocketchannel获得客户端对象
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8899));
        //创建选择器
        Selector selector = Selector.open();
        //服务器端关注连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        //开始进行事件处理
        while (true) {
            int numbers = selector.select();//关注事件数量
            Set<SelectionKey> selecedKeys = selector.selectedKeys();

            selecedKeys.forEach(selectedKey -> {
                try {
                    final SocketChannel client;
                    if (selectedKey.isAcceptable()) {//客户端向服务器端发起连接
                        ServerSocketChannel server = (ServerSocketChannel) selectedKey.channel();
                        client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        //客户端信息记录到服务器端后续才能分发
                        String key = "【" + UUID.randomUUID() + "】";
                        map.put(key, client);
                    } else if (selectedKey.isReadable()) {//服务器接受请求
                        client = (SocketChannel) selectedKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                        byteBuffer.clear();
                        int read = client.read(byteBuffer);
                        if (read > 0) {
                            //回写客户端
                            byteBuffer.flip();
                            Charset charset = Charset.forName("utf-8");
                            String receive = String.valueOf(charset.decode(byteBuffer).array());
                            System.out.println(client + ":" + receive);
                            String sendKey = null;
                            for (Map.Entry<String, SocketChannel> entry : map.entrySet()) {
                                if (entry.getValue().equals(client)) {
                                    sendKey = entry.getKey();
                                    break;
                                }
                            }
                            for (Map.Entry<String, SocketChannel> entry : map.entrySet()) {
                                SocketChannel value = entry.getValue();
                                ByteBuffer byteBuffer1 = ByteBuffer.allocate(1000);
                                byteBuffer1.put((sendKey + ":" + receive).getBytes());
                                byteBuffer1.flip();
                                value.write(byteBuffer1);
                            }
                        }

                    }

                } catch (Exception e) {

                }

                selecedKeys.clear();
            });

        }
    }

}
