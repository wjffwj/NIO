package six.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 0拷贝
 */
@SuppressWarnings("all")
public class NioTest9 {
    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream = new FileInputStream("input.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("output.txt");

        FileChannel inputChannel = fileInputStream.getChannel();
        FileChannel outputChannel = fileOutputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocateDirect(512);//分配直接内存 0拷贝
        while (true) {
            buffer.clear();
            int read = inputChannel.read(buffer);
            if (-1 == read) {
                break;
            }
            buffer.flip();
            outputChannel.write(buffer);

        }

        inputChannel.close();
        outputChannel.close();
    }
}
