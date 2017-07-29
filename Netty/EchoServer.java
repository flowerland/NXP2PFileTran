package JavaIO.Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Created by liuzhichao on 2017/7/27.
 * netty服务端
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }
    /**
     * • 创建ServerBootstrap实例来引导绑定和启动服务器 <br>
     * • 创建NioEventLoopGroup对象来处理事件，如接受新连接、接收数据、写数据等等 <br>
     * • 指定InetSocketAddress，服务器监听此端口<br>
     * • 设置childHandler执行所有的连接请求 <br>
     * • 都设置完毕了，最后调用ServerBootstrap.bind() 方法来绑定服务器<br>
     *
     * @throws Exception
     */
    public void start() throws Exception{
        // 所以指定NioEventLoopGroup来接受和处理新连接
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            /**
             * 先看看ServerBootstrap提供了哪些方法<br>
             * • group(...)，设置EventLoopGroup事件循环组<br>
             * • channel(...)，设置通道类型<br>
             * • channelFactory(...)，使用ChannelFactory来设置通道类型<br>
             * • localAddress(...)，设置本地地址，也可以通过bind(...)或connect(...)<br>
             * • option(ChannelOption<T>,
             * T)，设置通道选项，若使用null，则删除上一个设置的ChannelOption<br>
             * • childOption(ChannelOption<T>, T)，设置子通道选项<br>
             * • attr(AttributeKey<T>, T)，设置属性到Channel，若值为null，则指定键的属性被删除<br>
             * • childAttr(AttributeKey<T>, T)，设置子通道属性<br>
             * • handler(ChannelHandler)，设置ChannelHandler用于处理请求事件<br>
             * • childHandler(ChannelHandler)，设置子ChannelHandler<br>
             * • clone()，深度复制ServerBootstrap，且配置相同<br>
             * • bind(...)，创建一个新的Channel并绑定<br>
             */
            ServerBootstrap b = new ServerBootstrap();
            b.group(group);
            b = b.channel(NioServerSocketChannel.class);// 指定通道类型为NioServerSocketChannel
            b = b.localAddress(port);// 设置InetSocketAddress让服务器监听某个端口已等待客户端连接。
            // 接下来，调用childHandler放来指定连接后调用的ChannelHandler
            b.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new EchoServerHandler());
                }
            });
            // 最后绑定服务器等待直到绑定完成，调用sync()方法会阻塞直到服务器完成绑定
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName()
                    + "started and listen on “" + f.channel().localAddress());
            // 然后服务器等待通道关闭，因为使用sync()，所以关闭操作也会被阻塞
            f.channel().closeFuture().sync();
        }finally {
            // 现在你可以关闭EventLoopGroup和释放所有资源，包括创建的线程。
            group.shutdownGracefully().sync();// 因为使用sync()，所以关闭操作也会被阻塞
        }
    }
    public static void main(String[] args) throws Exception {
        new EchoServer(65535).start();
    }
}
