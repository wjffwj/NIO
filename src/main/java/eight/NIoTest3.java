package eight;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.RandomAccess;

/**
 * 文件编解码
 */
public class NIoTest3 {
    public static void main(String[] args)  throws Exception{
        String inputFile ="NioTest3_In.txt";
        String outputFile = "NioTest3_out.txt";

        RandomAccessFile randomInputFile = new RandomAccessFile(inputFile,"r");
        RandomAccessFile randomOutpuFile = new RandomAccessFile(outputFile,"rw");
        long inputFileLength = new File(inputFile).length();
        FileChannel fileInChannel = randomInputFile.getChannel();
        FileChannel fileOutChannel = randomOutpuFile.getChannel();
        MappedByteBuffer inputData = fileInChannel.map(FileChannel.MapMode.READ_ONLY,0,inputFileLength);
        Charset charset =Charset.forName("iso-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();
        CharBuffer charBuffer = decoder.decode(inputData);
        ByteBuffer byteBuffer = encoder.encode(charBuffer);
        fileOutChannel.write(byteBuffer);
    }
}
