package first;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.UUID;

/**
 * 自定义的处理器
 */
public class MyServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     *
     * @param ctx 上下文信息
     * @param s 客户端的请求参数
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        //打印远程地址和请求信息
        System.out.println(ctx.channel().remoteAddress()+": 请求内容是"+s);
        //返回客户端数据
        ctx.channel().writeAndFlush("from server "+ UUID.randomUUID());


    }

    /**
     * 服务端出现了异常该做什么 ，一般是将连接关闭掉
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
