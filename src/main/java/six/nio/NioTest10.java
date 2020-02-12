package six.nio;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

//内存映射文件 DireactByteBuffer->MappedByteBuffer->ByteBuffer
// MappedByteBuffer  位于堆外内存
public class NioTest10 {

    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile("NioTest10.txt", "rw");
        FileChannel channel = file.getChannel();
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);//将文件映射到内存中， 通过修改内存就可以修改文件,具体修改如何同步到磁盘交给操作系统
        mappedByteBuffer.put(0, (byte) 'a');
        mappedByteBuffer.put(3, (byte) 'b');
        file.close();
    }

}
