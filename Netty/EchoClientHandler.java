package JavaIO.Netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by liuzhichao on 2017/7/27.
 * 客户端的业务逻辑的实现依然很简单，更复杂的用法将在后面章节详细介绍。和编写服务器的ChannelHandler一样，
 * 在这里将自定义一个继承SimpleChannelInboundHandler的ChannelHandler来处理业务 ；<br>
 * 通过重写父类的三个方法来处理感兴趣的事件：<br>
 * • channelActive()：客户端连接服务器后被调用 <br>
 * • channelRead0()：从服务器接收到数据后调用 <br>
 * • exceptionCaught()：发生异常时被调用<br>
 */
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf>{
    /**
     * 可能你会问为什么在这里使用的是SimpleChannelInboundHandler而不使用ChannelInboundHandlerAdapter?
     * 主要原因是ChannelInboundHandlerAdapter在处理完消息后需要负责释放资源,在这里将调用ByteBuf.release()来释放资源
     * SimpleChannelInboundHandler会在完成channelRead0后释放消息,
     * ，这是通过Netty处理所有消息的ChannelHandler实现了ReferenceCounted接口达到的。
     * 为什么在服务器中不使用SimpleChannelInboundHandler呢？
     * 因为服务器要返回相同的消息给客户端 ，在服务器执行完成写操作之前不能释放调用读取到的消息，
     * 因为写操作是异步的，一旦写操作完成后，Netty中会自动释放消息。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        System.out.println("active");
        ctx.write(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println("Client received: "+ ByteBufUtil.hexDump(msg.readBytes(msg.readableBytes())));
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    protected void messageReceived(ChannelHandlerContext var1, ByteBuf var2) throws Exception{
        System.out.println("Client received1: "+ ByteBufUtil.hexDump(var2.readBytes(var2.readableBytes())));
    }
}
