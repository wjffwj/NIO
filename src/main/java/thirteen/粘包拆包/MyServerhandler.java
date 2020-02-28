package thirteen.粘包拆包;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

//TCP粘包问题 服务器端看到的是字节流 ，他把这个字节流当成1条消息了

//如何解决粘包拆包问题需要通过编解码器进行处理
public class MyServerhandler extends SimpleChannelInboundHandler<ByteBuf> {
    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);
        String message = new String(buffer, StandardCharsets.UTF_8);
        System.out.println("服务端收到的消息是" + message);
        System.out.println("服务端收到的消息数量" + (++this.count));
        ByteBuf respByteBuf = Unpooled.copiedBuffer(UUID.randomUUID().toString(), StandardCharsets.UTF_8);
        ctx.writeAndFlush(respByteBuf);
    }
}
