package NXP2P_FileTran;



import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by liuzhichao on 2017/7/27.
 * Server注册时间监听操作
 */
public class P2PServer {
    private JTextArea jta;
    private ServerFrame server;
    // 通道管理器
    private Selector selector;
    private ArrayList<String> arrayFile = new ArrayList<String>();
    public P2PServer(ServerFrame Frame) throws IOException{
        this.server = Frame;
        this.jta = Frame.textArea;
        go();
    }

    public static void main(String[] args) throws IOException,InterruptedException{
        ServerFrame window = new ServerFrame();
        window.frame.setVisible(true);
        P2PServer serverOpe = new P2PServer(window);
        serverOpe.listen();

    }
    private void go() throws IOException {
        int port = Integer.parseInt(server.textField_port.getText().trim());
        // 打开监听信道
        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
        // 与本地端口绑定
        listenerChannel.socket().bind(new InetSocketAddress(port));
        jta.append("Napd server started at " + new Date() + "\n");
        // 设置为非阻塞模式
        listenerChannel.configureBlocking(false);
        // 创建选择器
        this.selector = Selector.open();
        // 将选择器绑定到监听信道,只有非阻塞信道才可以注册选择器.并在注册过程中指出该信道可以进行Accept操作
        //一个server socket channel准备好接收新进入的连接称为“接收就绪”
        listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    private void listen() throws IOException,InterruptedException {
        int clientNo = 1;
        while (this.selector.select()>0) {
            // 当注册事件到达时，方法返回，否则该方法会一直阻塞
            // 获得selector中选中的相的迭代器，选中的相为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已选的key 以防重负处理
                ite.remove();
                if (key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获得和客户端连接的通道
                    SocketChannel clientChannel = server.accept();
                    // 设置成非阻塞
                    clientChannel.configureBlocking(false);
                    Socket socket = clientChannel.socket();
                    jta.append("Strating thread for client" + clientNo + " at " + new Date() +"\n");
                    InetAddress inetAddress = socket.getInetAddress();
                    jta.append("Client" + clientNo + "s host name is " + inetAddress.getHostName() + "Port: "+ socket.getPort() +"\n");
                    jta.append("Client" + clientNo + "s IP Address is " + inetAddress.getHostAddress() + "Port: "+ socket.getPort() +"\n");
                    clientChannel.write(ByteBuffer.wrap(new String("SUCCESS connect success").getBytes()));
                    clientChannel.register(selector, SelectionKey.OP_READ); // 获得了可读的事件
                    //clientNo++;
                }else if (key.isValid() && key.isReadable()) {
                    read(key);
                }
            }
        }
    }
    // 处理 读取客户端发来的信息事件
    private void read(SelectionKey key) throws IOException,InterruptedException {
        // 服务器可读消息，得到事件发生的socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        Socket socket = channel.socket();
        InetAddress inetAddress = socket.getInetAddress();
        // 穿件读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try{
            channel.read(buffer);
        }catch(IOException e){
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }
        byte[] data = buffer.array();
        String response = new String(data).trim();
        System.out.println("server receive from client: " + response);
        buffer.clear();
        String[] respArray = response.split(" ");
        if(respArray[0].equals("REQUEST") && respArray.length>1)
        {

            if(arrayFile.isEmpty())
                channel.write(ByteBuffer.wrap(new String("REQUEST ERROR " +  "does not have share file").getBytes()));
            else if(respArray[1].isEmpty())
                channel.write(ByteBuffer.wrap(new String("REQUEST ERROR " +  "file name is empty").getBytes()));
            else{
                int i=0;
                for(;i<arrayFile.size();i++)
                {
                    String shareResources = arrayFile.get(i);
                    String[] str = shareResources.split(" ");
                    if(respArray[1].equals(str[1])){
                        channel.write(ByteBuffer.wrap(new String("REQUEST " + str[0] + " " + str[3] + " " + str[1]).getBytes()));
                        break;
                    }
                }
                if(i>=arrayFile.size())
                {
                    channel.write(ByteBuffer.wrap(new String("REQUEST OK").getBytes()));
                }
            }
        }else if(response.equals("CONNECT"))
            channel.write(ByteBuffer.wrap(new String("ACCEPT").getBytes()));
         else if(response.equals("LIST")){
            String request = "";
            String[] reqArray;
            if(arrayFile.isEmpty())
            {
                channel.write(ByteBuffer.wrap(new String("NO file shareNO file share").getBytes()));
            }
            else
            {
                for(int i=0;i<arrayFile.size();i++)
                {
                    request = arrayFile.get(i);
                    reqArray = request.split(" ");
                    channel.write(ByteBuffer.wrap(new String("LISTFILE " +reqArray[1] + " " + reqArray[3]).getBytes()));
                    Thread.sleep(50);
                }
                channel.write(ByteBuffer.wrap(new String("LISTFILE OK").getBytes()));
            }
        }else if (respArray[0].equals("END")){
            channel.write(ByteBuffer.wrap(new String("ENTER>>").getBytes()));
        }
        else if(respArray[0].equals("ADD")){
            if(!respArray[1].isEmpty())
            {
                String storageFlie = "";
                storageFlie +=  inetAddress.getHostAddress() + " ";
                for(int i=1;i<respArray.length;i++)
                {
                    storageFlie += respArray[i]  + " ";

                }
                if(arrayFile.contains(storageFlie))
                {
                    channel.write(ByteBuffer.wrap(new String("ERROR " + respArray[1] + "already exit").getBytes()));
                }
                else
                {
                    arrayFile.add(storageFlie);
                    //channel.write(ByteBuffer.wrap(new String("OK").getBytes()));
                }

            }else
            {
                channel.write(ByteBuffer.wrap(new String("OERROR "  + "file name is empty").getBytes()));
            }
        }else if(respArray[0].equals("DELETE")){
            if(!respArray[1].isEmpty())
            {
                String storageFlie = "";
                storageFlie +=  inetAddress.getHostAddress() + " ";
                for(int i=1;i<respArray.length;i++)
                {
                    storageFlie += respArray[i]  + " ";
                }
                if(arrayFile.contains(storageFlie))
                {
                    arrayFile.remove(storageFlie);
                    channel.write(ByteBuffer.wrap(new String("OK").getBytes()));
                }else
                {
                    channel.write(ByteBuffer.wrap(new String("ERROR "  + "you don't share the " + respArray[1]).getBytes()));
                }
            }else{
                channel.write(ByteBuffer.wrap(new String("OERROR "  + "file name is empty").getBytes()));
            }
        }else if(respArray[0].equals("QUIT")){
            channel.write(ByteBuffer.wrap(new String("GOODBYE").getBytes()));
            channel.close();
        }else{
            channel.write(ByteBuffer.wrap(new String("ERROR " + "input is false").getBytes()));
        }
    }
}
