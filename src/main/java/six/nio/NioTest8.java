package six.nio;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

//只读buffer
public class NioTest8 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.clear();
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put((byte) i);
        }
        ByteBuffer readOnlyBuffer = buffer.asReadOnlyBuffer();

        System.out.println(buffer.getClass());
        System.out.println(readOnlyBuffer.getClass());
        for (int i = 0; i < readOnlyBuffer.capacity(); i++) {
            System.out.println(readOnlyBuffer.get(i));
        }
    }
}
