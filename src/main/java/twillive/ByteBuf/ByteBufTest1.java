package twillive.ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteBufTest1 {
    public static void main(String[] args)  throws Exception{
        ByteBuf buffer = Unpooled.copiedBuffer("魏hello,world", StandardCharsets.UTF_8);// utf8 3字节表示1个字符
        //Returns {@code true} if and only if this buffer has a backing byte array.*
        // If this method returns true, you can safely call {@link #array()} and
        if(buffer.hasArray()) {
           byte[] content  = buffer.array();//返回buffer的字节数组
            System.out.println(new String(content,StandardCharsets.UTF_8));
        }
        System.out.println(buffer);
        //UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeHeapByteBuf(ridx: 0, widx: 11, cap: 33)'
        //数组偏移量  Returns the offset of the first byte within the backing byte array of     * this buffer.
        System.out.println(buffer.arrayOffset());

        System.out.println(buffer.readerIndex());
        System.out.println(buffer.writerIndex());
        System.out.println(buffer.capacity());

        System.out.println(buffer.readableBytes());//writerIndex-readerIndex

        for(int i=0;i<buffer.readableBytes();++i) {
            System.out.println((char)(buffer.getByte(i)));
        }
        System.out.println(buffer.getCharSequence(0,3, StandardCharsets.UTF_8));
    }
}
