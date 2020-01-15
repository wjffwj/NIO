package NIO;

import java.nio.ByteBuffer;

/**
 * @created by 24745
 * @date 2020/1/15
 */

public class BasicBuffer {
    public static void main(String[] args) {
        //举例说明buffer的使用 position , limit , captity
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        String str = "Hello,world";
        byteBuffer.put(str.getBytes());
        System.out.println(1);

    }
}
