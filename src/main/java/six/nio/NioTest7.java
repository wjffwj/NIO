package six.nio;

import java.nio.ByteBuffer;

/**
 * 分片buffer  slice 会影响原buffer的数据
 */
public class NioTest7 {

    public static void main(String[] args) {
    ByteBuffer buffer = ByteBuffer.allocate(10);

        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put((byte) i);
        }

        buffer.position(2);
        buffer.limit(6);
        ByteBuffer slinceBuffer = buffer.slice();


        for (int i = 0; i < slinceBuffer.capacity(); i++) {

            byte b = slinceBuffer.get(i);
            b *= 2;
            slinceBuffer.put(i, b);
        }
        buffer.position(0);
        buffer.limit(buffer.capacity());

        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
    }
}
