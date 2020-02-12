package six.nio;

import java.nio.IntBuffer;
import java.security.SecureRandom;

public class NioTest4 {
    public static void main(String[] args) {
        IntBuffer buffer =IntBuffer.allocate(10);
        for(int i=0;i<5;i++) {
            buffer.put(new SecureRandom().nextInt(10));
        }

        System.out.println( "before flip limit:"+buffer.limit());//10

        buffer.flip();
        System.out.println("after flip limit"+buffer.limit());//5


        while(buffer.hasRemaining()) {
            System.out.println("position"+buffer.position());
            System.out.println("limit:"+buffer.limit());
            System.out.println("capicity"+buffer.capacity());
            System.out.println(buffer.get());
        }

    }
}
