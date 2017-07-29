package p2p;
import java.io.*;
import java.net.*;
//用于计算 hash值
import org.apache.commons.codec.digest.DigestUtils;
class Global
{
	public static String path = "";
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
								File peerfile = new File(path + File.separator + respArray[1]);
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
//					else if(response.equals("CLOSE"))
//					{
//						continue;
//					}
				}
				//out.println("GOODBYE");
				//out.flush();
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

public class Nap
{
	public static void error_handler(String err)
	{
		System.out.println("\\033[1;31m[错误] >>\\033[0m " + err.substring(6));
		System.exit(-1);
	}
	// Main method
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Nap 客户端");
			Socket socket;
			BufferedReader in;
			PrintWriter out;
			// 初始化用于接收用户输入的 stdin
			BufferedReader  stdin  =  new  BufferedReader(new InputStreamReader(System.in));
			String server;//P2P 服务器 IP
			int port;//P2P 服务器端口
			String path;//本地 P2P 工作目录
			String request = "";
			String[] reqArray;
			String response;
			String[] respArray;
			//获取几个必要信息
			System.out.print("服务器的 IP 地址 >> ");
			server = stdin.readLine();
			
			//server = "127.0.0.1";
			
			System.out.print("服务器的端口号  >> ");
			port = Integer.parseInt(stdin.readLine());
			//port = 7777;
			
			System.out.print("本机的工作目录 >> ");
			path = stdin.readLine();
			//path = "E:\\我的资料\\优化算法";
			Global.path = path;
			
			socket = new Socket(server, port);
			System.out.println("local port:" + socket.getLocalPort());
			
			in  =  new  BufferedReader(new InputStreamReader(socket.getInputStream()));//服务器返回的消息
			out = new PrintWriter(socket.getOutputStream(), false);//发送给服务器都消息
			// 打印服务器返回的消息
			System.out.println(in.readLine());
			// 发送握手消息"CONNECT"开始握手
			out.println("CONNECT");
			out.flush();
			response = in.readLine();
			
			// 接收确认信息
			if(!response.equals("ACCEPT"))
			{
				System.out.println("\\033[1;31m[错误] >>\\033[0m 向服务器发送的握手消息未能接收到正确的确认包");
				System.exit(-1); 
			}
			else
			{
				System.out.println("\\033[1;32m[成功] >>\\033[0m 成功连接到 Napd服务器 " + server + ":" + port);
			}
			File folder = new File(path);
			File[] files = folder.listFiles();
			FileInputStream f_stream;
			String filename;
			String filehash =  "";
			String filesize;
			System.out.println("[信息] 正在为工作目录 " + path + " 建立文件索引...");
			int index_total = 0;
			for(int i = 0; i < files.length; i++)
			{
				if(files[i].isFile())
				{
					filename = files[i].getName();
					System.out.println(files[i].getAbsolutePath());
					f_stream = new FileInputStream(files[i]);
					filehash = DigestUtils.md5Hex(f_stream);
					f_stream.close();
					filesize = String.valueOf(files[i].length());
					out.println("ADD " + filename + " " + filehash + " " + filesize);
					out.flush();
					response = in.readLine();
					if(!response.equals("OK"))
						error_handler(response);
					else
					{
						System.out.print(". ");
						index_total++;
					}
				}
			}
			System.out.println("\\n\\033[1;32m[ 成功 ] >>\\033[0m 成功添加 " + index_total + " 信息到服务器");
			// 开启文件服务器线程
			Runnable run = new peer_server();
			Thread thread = new Thread(run);
			thread.start();
			System.out.println("[信息] 等待用户输入");
			do
			{
				System.out.print(">> ");
				request = stdin.readLine();
				reqArray = request.split(" ");
				if(request.equals("list"))
				{
					System.out.println("[信息] 正在向服务器请求文件列表...");
					// 发送 LIST 命令
					out.println("LIST");
					out.flush();
					int list_total = 0;
					response = in.readLine();
					respArray = response.split(" ");
					while((!respArray[0].equals("OK"))  && (!respArray[0].equals("ERROR")))
					{
						list_total++; 
						System.out.println(String.format("[%2d] : %20s [文件大小: %10s]", new Object[] { new Integer(list_total), respArray[0], respArray[1] }));
						response = in.readLine();
						respArray = response.split(" ");
					}
					System.out.println("[信息] 一共获取到 " + list_total + " 个文件");
					if(!response.equals("OK"))
						error_handler(response);
				}
				else if(reqArray[0].equals("request"))
				{
					try
					{
						if(!reqArray[1].isEmpty())
						{
							//发送 REQUEST
							out.println("REQUEST" + " " + reqArray[1]);
							out.flush();
							System.out.println("REQUEST" + " " + reqArray[1]);
							response = in.readLine();
							respArray = response.split(" ");
							
							System.out.println(response);
							
							if(respArray[0].equals("OK"))
								System.out.println("\\033[1;31m[错误] >>\\033[0m在服务器上未找到文件 " + reqArray[1]);
							while((!respArray[0].equals("OK"))  &&
									(!respArray[0].equals("ERROR")))
							{
								//respArray 格式；peer的IP+文件大小
								Socket comSocket = new Socket(respArray[0],7701);
								
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
								
								Socket fileSocket = new Socket(respArray[0],7702);
								comOut.println("GET" + " " + reqArray[1]);
								comOut.flush();
								
								InputStream fileIn = fileSocket.getInputStream();
								File f = new File(path+File.separator+"recv");
								if (!f.exists())
								{
									f.mkdirs();
								}
								BufferedOutputStream  fileOut  =  new BufferedOutputStream(new FileOutputStream(path+File.separator+"recv"+File.separator +reqArray[1]));
								int bytesRead,current = 0;
								byte[]  buffer  =  new byte[Integer.parseInt(respArray[1])]; 
								bytesRead = fileIn.read(buffer, 0, buffer.length);
								current = bytesRead;
								System.out.println("[信息] 开始传输文件...");
								do
								{
									System.out.print(". ");
									bytesRead  =  fileIn.read(buffer,  current, (buffer.length - current));
									if(bytesRead >= 0)
										current += bytesRead;
								} while(bytesRead > -1 && buffer.length !=current);
								fileOut.write(buffer, 0, current);
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
								//response = in.readLine();
								//respArray = response.split(" "); 
							}
							if(!respArray[0].equals("OK"))
								error_handler(response);
						}
					}
					catch (Exception e)
					{
						System.out.println("\\033[1;31m[错误] >>\\033[0m "+e);
					}
					
				}else if(reqArray[0].equals("delete"))
				{
					
					if(!reqArray[1].isEmpty())
					{
						File file = new File(Global.path + File.separator + reqArray[1]);
			
						FileInputStream f_stream1 = new FileInputStream(file);
						
						filehash = DigestUtils.md5Hex(f_stream1);
						f_stream1.close();
						filesize = String.valueOf(file.length());
						
						out.println("DELETE" + " "+ reqArray[1] + " " +  filehash + " " + filesize);
						out.flush();
						response = in.readLine();
						respArray = response.split(" ");
						if(!respArray[0].equals("OK"))
						{
							error_handler(response);
						}else
						{
							System.out.println("file remove success!!!!!!!!!!!!!!!!");
						}
						
					}else
					{
						System.out.println("please input file name");
					}
					
				}
				else if(!request.equals("quit"))
				{
					System.out.println("input is not true !!!!!!!!!!");
					System.out.println("Please input <list,request,delete,quit(quit is disconnected)>");
				}
			} while(!request.equals("quit"));
			out.println("QUIT");
			out.flush();
			response = in.readLine();
			if(!response.equals("GOODBYE"))
			{
				System.out.println("\\033[1;31m[错误] >>\\033[0m 程序未正常退出" + response);
				System.exit(-1);
			}
			else 
			{
				System.out.println("\\033[1;32m[成功] >>\\033[0m 成功关闭连接");
			}
			in.close();
			out.close();
			socket.close();
		}
		catch (Exception e)
		{
			System.out.println("\\033[1;31m[错误] >>\\033[0m "+e);
		}
	}
}


