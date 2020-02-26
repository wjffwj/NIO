package twillive.ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Iterator;

public class BytebuffTest2 {
    public static void main(String[] args) {
        CompositeByteBuf buffer = Unpooled.compositeBuffer();
        ByteBuf heapBuf = Unpooled.buffer(10);        //UnpooledUnsafeHeapByteBuf
        ByteBuf directBuf = Unpooled.directBuffer(8); //UnpooledUnsafeNoCleanerDirectByteBuf
        buffer.addComponent(heapBuf);
        buffer.addComponent(directBuf);
        Iterator<ByteBuf> iterator =buffer.iterator();
        while(iterator.hasNext()){
            ByteBuf buf = iterator.next();
            System.out.println(buf);
        }
    }
}
