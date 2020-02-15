package seven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioClient {
    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("localhost", 8899));

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    if (selectionKey.isConnectable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        if (client.isConnectionPending()) {//连接操作是否正在进行
                            client.finishConnect();//连接真正建立好

                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            byteBuffer.put((LocalDateTime.now() + "连接成功").getBytes());
                            byteBuffer.flip();
                            client.write(byteBuffer);
                            ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                            executorService.submit(() -> {
                                while (true) {
                                    try {
                                        byteBuffer.clear();
                                        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                        String sendMessage = bufferedReader.readLine();
                                        byteBuffer.put(sendMessage.getBytes());
                                        byteBuffer.flip();
                                        client.write(byteBuffer);
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        }
                        client.register(selector, SelectionKey.OP_READ);

                    } else if (selectionKey.isReadable()) {
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        int read = client.read(readBuffer);
                        if (read > 0) {
                            String receiveMesage = new String(readBuffer.array(), 0, read);
                            System.out.println(receiveMesage);
                        }
                    }

                }
                selectionKeys.clear();
            }


        } catch (Exception e) {

        }
    }
}
