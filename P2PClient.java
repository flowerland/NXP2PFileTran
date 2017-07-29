package NXP2P_FileTran;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by liuzhichao on 2017/7/27.
 */

class Global
{
    public static String path = "";
    public static Integer file_len = 0;
}

class peer_server implements Runnable
{
    private ServerSocket comServSock;
    private ServerSocket fileServSock;
    private String path;
    // 实现一个基本的文件服务器
    public peer_server()
    {

        try {
            //预定义
            path = Global.path;
            // 建立用于接受服务器消息的 ServerSocket
            comServSock = new ServerSocket(7701);
            // 建立用于接受另一个peer消息、传输文件的 ServerSocket
            fileServSock = new ServerSocket(7702);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("\\033[1;31m[错误----------------------------] >>\\033[0m "+e);
            e.printStackTrace();
        }

    }
    public void run()
    {
        try
        {

            while(true)
            {
                Socket socket = comServSock.accept();
                //创建输入流
                BufferedReader  in  =  new  BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), false);
                String response = "";
                String[] respArray;
                // 循环监听链接请求，直到接收到"HELLO"或者"QUIT"握手消息
                while(!response.equals("HELLO") && !response.equals("QUIT"))
                {
                    response = in.readLine();
                    System.out.println(response);
                    // 如果接收到的握手消息是 OPEN，回复确认消息 HELLO
                    if(response.equals("HELLO"))
                    {
                        out.println("ACCEPT");
                        out.flush();
                    }
                }
                // 循环监听链接请求 ，直到收到QUIT握手消息
                while(!response.equals("QUIT"))
                {
                    response = in.readLine();
                    System.out.println(response);
                    //请求参数分段处理
                    respArray = response.split(" ");
                    // syntax: GET [filename]
                    if(respArray[0].equals("GET"))
                    {
                        try
                        {
                            // 请求文件名不为空
                            if(!respArray[1].isEmpty())
                            {
                                // 新建一个用于文件传输的 socket
                                Socket fileSocket = fileServSock.accept();
                                String path = "E:\\个人简历";
                                File peerfile = new File(path + File.separator + respArray[1]);
                                System.out.println(response);
                                byte[] buffer = new byte[(int)peerfile.length()];
                                BufferedInputStream  fileIn  =  new BufferedInputStream(new FileInputStream(peerfile));
                                fileIn.read(buffer, 0, buffer.length);
                                BufferedOutputStream  fileOut  =  new BufferedOutputStream(fileSocket.getOutputStream());
                                fileOut.write(buffer, 0, buffer.length);
                                fileOut.flush();
                                fileIn.close();
                                fileOut.close();
                                fileSocket.close();
                                out.println("OK");
                                out.flush();
                            }
                        }
                        catch (Exception e)
                        {
                            out.println("ERROR "+e);
                            out.flush();
                            response = "QUIT";
                        }
                    }
                }
                in.close();
                out.close();
                socket.close();
            }
        }
        catch (Exception e)
        {
            System.out.println("\\033[1;31m[错误----------------------------] >>\\033[0m "+e);
            System.exit(-1);
        }
    }
}

public class P2PClient {
    //信道选择器
    private Selector selector;
    // 与服务器通信的信道
    SocketChannel socketChannel;
    // 要连接的服务器Ip地址
    private String hostIp;
    // 要连接的远程服务器在监听的端口
    private int hostListenningPort;

    public P2PClient(String HostIp, int HostListenningPort) throws IOException {
        this.hostIp = HostIp;
        this.hostListenningPort = HostListenningPort;

        initialize();
    }

    private void initialize() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);// 设置通道为非阻塞
        this.selector = Selector.open();  // 获得一个通道管理器
        socketChannel.connect(new InetSocketAddress(hostIp, hostListenningPort));

        socketChannel.register(selector, SelectionKey.OP_CONNECT);

    }

    public static void error_handler(String err)
    {
        System.out.println("\\033[1;31m[错误] >>\\033[0m " + err.substring(6));
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        Runnable run = new peer_server();
        Thread thread = new Thread(run);
        thread.start();
        // 初始化用于接收用户输入的 stdin
        BufferedReader  stdin  =  new  BufferedReader(new InputStreamReader(System.in));
        String server;//P2P 服务器 IP
        int port;//P2P 服务器端口
        String path;//本地 P2P 工作目录
        //获取几个必要信息
        System.out.print("服务器的 IP 地址 >> ");
        server = stdin.readLine();
        //server = "127.0.0.1";

        System.out.print("服务器的端口号  >> ");
        port = Integer.parseInt(stdin.readLine());
        //port = 8000;

        System.out.print("本机的工作目录 >> ");
        path = stdin.readLine();
        //path = "E:\\个人简历";
        Global.path = path;
        Global.file_len = 0;
        //P2PClient client = new P2PClient("127.0.0.1", 8000);
        P2PClient client = new P2PClient(server, port);
        client.listen();
    }
    public void listen() throws IOException{
        Selector selector = this.selector;
        while (selector.select() > 0) {
            Iterator ite = selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next(); // 删除已选的key,以防重复处理
                ite.remove(); // 连接事件发生
                if (key.isConnectable()) {
                    SocketChannel channel = (SocketChannel) key.channel(); // 如果正在连接，则完成连接
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                    } // 设置成非阻塞
                    channel.configureBlocking(false);
                    // 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
                    channel.register(selector, SelectionKey.OP_READ); // 获得了可读的事件
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel serverChannel = (SocketChannel) key.channel();
        Socket socket = serverChannel.socket();
        InetAddress inetAddress = socket.getInetAddress();
        // 穿件读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        serverChannel.read(buffer);
        byte[] data = buffer.array();
        String response = new String(data).trim();
        String[] respArray = response.split(" ");
        buffer.clear();
        if (response.equals("SUCCESS connect success")){
            System.out.println(response);
            serverChannel.write(ByteBuffer.wrap(new String("CONNECT").getBytes()));
        }
        else if (response.equals("ENTER>>")) {
            System.out.println(response);
            String request = "";
            String[] reqArray;
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            request = stdin.readLine();
            reqArray = request.split(" ");
            if (request.equals("list")) {
                System.out.println("[信息] 正在向服务器请求文件列表...");
                // 发送 LIST 命令
                serverChannel.write(ByteBuffer.wrap(new String("LIST").getBytes()));
            } else if (reqArray[0].equals("request")) {
                if (!reqArray[1].isEmpty()) {
                    //发送 REQUEST
                    serverChannel.write(ByteBuffer.wrap(new String("REQUEST" + " " + reqArray[1]).getBytes()));
                    System.out.println("REQUEST" + " " + reqArray[1]);
                }
            }
        }else if (respArray[0].equals("REQUEST")){
            System.out.println(response);
            if(respArray[1].equals("OK"))
                System.out.println("\\033[1;31m[错误] >>\\033[0m在服务器上未找到文件 " + respArray[3]);
            if ((!respArray[1].equals("OK"))  && (!respArray[1].equals("ERROR")))
            {
                //respArray 格式；peer的IP+文件大小
                    Socket comSocket = new Socket(respArray[1],7701);

                    String comResponse;
                    String[] comRespArray;
                    BufferedReader comIn = new BufferedReader(new InputStreamReader(comSocket.getInputStream()));
                    PrintWriter  comOut  =  new PrintWriter(comSocket.getOutputStream(), false);
                    //验证身份
                    comOut.println("HELLO");
                    comOut.flush();
                    comResponse = comIn.readLine();
                    //确认
                    if(!comResponse.equals("ACCEPT"))
                    {
                        System.out.println("\\033[1;31m[错误] >>\\033[0m 客户端握手消息验证失败");
                        System.exit(-1);
                    }

                    Socket fileSocket = new Socket(respArray[1],7702);
                    comOut.println("GET" + " " + respArray[3]);
                    comOut.flush();
                    InputStream fileIn = fileSocket.getInputStream();
                    File f = new File(Global.path+File.separator+"recv");
                    if (!f.exists())
                    {
                        f.mkdirs();
                    }
                    BufferedOutputStream  fileOut  =  new BufferedOutputStream(new FileOutputStream(Global.path+File.separator+"recv"+File.separator +respArray[3]));
                    int bytesRead,current = 0;
                    byte[]  buffer_file  =  new byte[Integer.parseInt(respArray[2])];
                    bytesRead = fileIn.read(buffer_file, 0, buffer_file.length);
                    current = bytesRead;
                    System.out.println("[信息] 开始传输文件...");
                    do
                    {
                        System.out.print(". ");
                        bytesRead  =  fileIn.read(buffer_file,  current, (buffer_file.length - current));
                        if(bytesRead >= 0)
                            current += bytesRead;
                    } while(bytesRead > -1 && buffer_file.length !=current);
                    fileOut.write(buffer_file, 0, current);
                    fileOut.flush();


                    comResponse = comIn.readLine();
                    comRespArray = comResponse.split(" ");

                    if(comRespArray[0].equals("OK"))
                    {
                        System.out.println("\\n\\033[1;32m[成功] >>\\033[0m 文件传输成功");
                        comOut.println("QUIT");
                        comOut.flush();
                    }else
                    {
                        System.out.println("file transfer  have problem, please request again");
                        error_handler(comResponse);
                    }

                    //关闭文件传输流
                    fileIn.close();
                    fileOut.close();
                    fileSocket.close();

                    //关闭文件传输中的消息流
                    comOut.close();
                    comIn.close();
                    comSocket.close();


                    respArray[0] = "OK";//让程序退出本层循环
                }
            if(!respArray[0].equals("OK"))
                error_handler(response);
        }else if (response.equals("CONNECT")){
            System.out.println(response);
        }
        else if (respArray[0].equals("LISTFILE")){
            if ((!respArray[1].equals("OK"))  && (!respArray[1].equals("ERROR")))
            {
                Integer len = ++Global.file_len;
                System.out.println(String.format("[%2d] : %20s [文件大小: %10s]", new Object[] { len, respArray[1], respArray[2] }));
            }
            if(respArray[1].equals("OK"))
                serverChannel.write(ByteBuffer.wrap(new String("END").getBytes()));
        }
        else if (response.equals("ACCEPT"))
        {
                System.out.println("\\033[1;32m[成功] >>\\033[0m 成功连接到 Napd服务器 " + inetAddress.getHostAddress());
                File folder = new File(Global.path);
                File[] files = folder.listFiles();
                FileInputStream f_stream;
                String filename;
                String filehash = "";
                String filesize;
                System.out.println("[信息] 正在为工作目录 " + Global.path + " 建立文件索引...");
                int index_total = 0;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        filename = files[i].getName();
                        System.out.println(files[i].getAbsolutePath());
                        f_stream = new FileInputStream(files[i]);
                        filehash = DigestUtils.md5Hex(f_stream);
                        f_stream.close();
                        filesize = String.valueOf(files[i].length());
                        serverChannel.write(ByteBuffer.wrap(new String("ADD " + filename + " " + filehash + " " + filesize).getBytes()));
                        System.out.print(". ");
                        index_total++;
                    }
                }
                System.out.println("\\n\\033[1;32m[ 成功 ] >>\\033[0m 成功添加 " + index_total + " 信息到服务器");
                serverChannel.write(ByteBuffer.wrap(new String("END").getBytes()));
            }
    }

}
