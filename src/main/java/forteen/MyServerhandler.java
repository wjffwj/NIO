package forteen;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class MyServerhandler extends SimpleChannelInboundHandler<PersonProtocol> {
    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PersonProtocol msg) throws Exception {
        int length = msg.getLength();
        byte[] content = msg.getContent();
        System.out.println("服务端收到数据长度" + length);
        System.out.println("服务端收到数据内容" + new String(content, StandardCharsets.UTF_8));
        System.out.println("服务端收到消息数量" + (++this.count));

        String resMsg = UUID.randomUUID().toString();
        int reslength = resMsg.getBytes(StandardCharsets.UTF_8).length;
        byte[] resContent = resMsg.getBytes(StandardCharsets.UTF_8);
        PersonProtocol personProtocol = new PersonProtocol();
        personProtocol.setLength(reslength);
        personProtocol.setContent(resContent);
        ctx.writeAndFlush(personProtocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
