package JavaIO.Netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by liuzhichao on 2017/7/27.
 * 创建启动一个客户端包含下面几步：<br>
 * • 创建Bootstrap对象用来引导启动客户端<br>
 * • 创建EventLoopGroup对象并设置到Bootstrap中，EventLoopGroup可以理解为是一个线程池，这个线程池用来处理连接、接受数据
 * 、发送数据<br>
 * • 创建InetSocketAddress并设置到Bootstrap中，InetSocketAddress是指定连接的服务器地址<br>
 * • 添加一个ChannelHandler，客户端成功连接服务器后就会被执行<br>
 * • 调用Bootstrap.connect()来连接服务器<br>
 * • 最后关闭EventLoopGroup来释放资源<br>
 */
public class EchoClient {
    private final String host;
    private final int port;
    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    /**
     * 创建Bootstrap实例使用new关键字，下面是Bootstrap的方法
     * • group(...)，设置EventLoopGroup,EventLoopGroup用来处理所有通道的IO事件
     * • channel(...)，设置通道类型
     * • channelFactory(...)，使用ChannelFactory来设置通道类型
     *  localAddress(...)，设置本地地址，也可以通过bind(...)或connect(...)
     *  • option(ChannelOption<T>, T)，设置通道选项，若使用null，则删除上一个设置的ChannelOption
     *  • attr(AttributeKey<T>, T)，设置属性到Channel，若值为null，则指定键的属性被删除
     *  • handler(ChannelHandler)，设置ChannelHandler用于处理请求事件
     *   • clone()，深度复制Bootstrap，Bootstrap的配置相同
     *   • remoteAddress(...)，设置连接地址
     *   • connect(...)，连接远程通道
     *    • bind(...)，创建一个新的Channel并绑定
     */
    public void start() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).
                    remoteAddress(new InetSocketAddress(host, port)).
                    handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception{
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();

        }finally {
            group.shutdownGracefully().sync();
        }
    }
    public static void main(String[] args) throws Exception {
        new EchoClient("127.0.0.1", 65535).start();
    }
}
