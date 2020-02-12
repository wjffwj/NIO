package six.nio;

import java.nio.IntBuffer;
import java.security.SecureRandom;

public class NioTest1 {
    public static void main(String[] args) {
        IntBuffer buffer = IntBuffer.allocate(10);//分配一个大小为10的缓冲区
        for (int i = 0; i < buffer.capacity(); i++) {
            int randomNumber = new SecureRandom().nextInt(20);//生成一个随机数
            buffer.put(randomNumber);//buffer写

        }
        buffer.flip();//buffer状态反转

        while (buffer.hasRemaining()) {//buffer读
            System.out.println(buffer.get());
        }
    }
}
