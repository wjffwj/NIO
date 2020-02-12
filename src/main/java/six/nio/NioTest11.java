package six.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/*
关于buffer的Scattering(散开)  和Gathing（多个合成1个）
 */
public class NioTest11 {

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(8899);
        serverSocketChannel.bind(address);

        int messageLength = 2 + 3 + 4;
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = ByteBuffer.allocate(2);
        buffers[1] = ByteBuffer.allocate(3);
        buffers[2] = ByteBuffer.allocate(4);

        SocketChannel socketChannel = serverSocketChannel.accept();
        while (true) {
            int bytesRead = 0;
            while (bytesRead < messageLength) {
                long r = socketChannel.read(buffers);
                bytesRead += r;
                System.out.println(bytesRead);

                Arrays.asList(buffers).stream().map(buffer -> "position:" + buffer.position() + ",limit:" + buffer.limit()).forEach(System.out::println);
            }
            Arrays.asList(buffers).forEach(buffer -> {
                buffer.flip();

            });
            long bytesWriten = 0;
            while(bytesWriten<messageLength) {
                long r = socketChannel.write(buffers);
                bytesWriten+=r;
            }
            for (ByteBuffer byteBuffer : Arrays.asList(buffers)) {
                byteBuffer.clear();
            }

        }

    }

}
