package six.nio;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NioTest3 {

    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("NioTest3.txt");
        FileChannel channel = fileOutputStream.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        byte[] messages = "hello,world".getBytes();
        for (int i = 0; i < messages.length; i++) {
            byteBuffer.put(messages[i]);
        }

        byteBuffer.flip();
        channel.write(byteBuffer);
        fileOutputStream.close();

    }

}
