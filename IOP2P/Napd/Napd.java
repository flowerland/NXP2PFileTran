import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.*;
import javax.swing.*;


public class Napd implements Runnable
{
	
	private JTextArea jta;
	private Server server;
//	private JTextField textField_port;
//	private JTextField textField_numberOfThreadPool;
	private ArrayList<String> arrayFile = new ArrayList<String>();
	public Napd(Server Frame)
	{
		this.server = Frame;
		this.jta = Frame.textArea;
	}
	public void run()
	{
		try{
//			this.server = Frame;
//			this.jta = Frame.textArea;
//			this.textField_port = textField_port;
//			this.textField_numberOfThreadPool = textField_numberOfThreadPool;
			
			
			
			int port = Integer.parseInt(server.textField_port.getText().trim());
			int numberOfThreadPool = Integer.parseInt(server.textField_numberOfThreadPool.getText().trim());
			
//			System.out.println(port);
//			System.out.println(numberOfThreadPool);
			
			ServerSocket serverSocket = new ServerSocket(port);
			jta.append("Napd server started at " + new Date() + "\n");
			
			//Number a client
			int clientNo = 1;
			ExecutorService execurtor = Executors.newFixedThreadPool(numberOfThreadPool);
			
			while(true)
			{
				if(this.server == null) break;
				Socket  socket =  serverSocket.accept();
				jta.append("Strating thread for client" + clientNo + " at " + new Date() +"\n");
				InetAddress inetAddress = socket.getInetAddress();
				jta.append("Client" + clientNo + "s host name is " + inetAddress.getHostName() + "Port: "+ socket.getPort() +"\n");
				jta.append("Client" + clientNo + "s IP Address is " + inetAddress.getHostAddress() + "Port: "+ socket.getPort() +"\n");
				HandleAclient task = new HandleAclient(socket);
				if(clientNo == 65)
				{
					String response = "";
					task.getOutputToClient().println("ERROR Thread Pool is not enough");
					task.getOutputToClient().flush();
					response = task.getInputFromClient().readLine();
					System.out.println(response);
					if(response.equals("CONNECT"))
					{
						task.getOutputToClient().println("ERROR connect failed");
						task.getOutputToClient().flush();
					}
					socket.close();
					break;
				}
				else
				{
					
					task.getOutputToClient().println("SUCCESS connect success");
					task.getOutputToClient().flush();
					//System.out.println("SUCCESS connect success");
				}
				execurtor.execute(task);
				clientNo++;
			}
			execurtor.shutdown();
			
		}catch(IOException ex){
			System.out.println(ex);
		}
	}
	
	class HandleAclient implements Runnable
	{
		private Socket socket;
		private BufferedReader  inputFromClient;
		private PrintWriter outputToClient;
		private InetAddress inetAddress;
		public HandleAclient(Socket socket)
		{
			this.socket = socket;
			this.inetAddress = socket.getInetAddress();
			try {
				inputFromClient = new  BufferedReader(new InputStreamReader(socket.getInputStream()));
				outputToClient = new PrintWriter(socket.getOutputStream(), false);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		public void run()
		{
			
			
				try{
						//System.out.println("thread start");
						
						String response;
						String[] respArray;
						while(true)
						{
							if(server == null) break;
							response  = this.inputFromClient.readLine();
							respArray = response.split(" ");				
							
							if(respArray[0].equals("REQUEST") && respArray.length>1)
							{
								
								if(arrayFile.isEmpty())
								{
									this.outputToClient.println("ERROR " +  "does not have share file");
									this.outputToClient.flush();
									
								}else if(respArray[1].isEmpty())
								{
									this.outputToClient.println("ERROR " +  "file name is empty");
									this.outputToClient.flush();
								}
								else
								{
									int i=0;
									for(;i<arrayFile.size();i++)
									{
										String shareResources = arrayFile.get(i);
										String[] str = shareResources.split(" ");
										//System.out.println(str[1]);
										if(respArray[1].equals(str[1]))
										{
											this.outputToClient.println(str[0] + " " + str[3]);
											this.outputToClient.flush();
											break;
										}
									}
									if(i>=arrayFile.size())
									{
										this.outputToClient.println("OK");//表示文件不存在
										this.outputToClient.flush();
									}
								}
			
							}else if(response.equals("CONNECT"))
							{
								this.outputToClient.println("ACCEPT");
								this.outputToClient.flush();
							}
							else if(response.equals("LIST"))
							{
								String request = "";
								String[] reqArray;
								if(arrayFile.isEmpty())
								{
									this.outputToClient.println("ERROR" + " " + "NO file share");
									this.outputToClient.flush();
								}
								else
								{
									for(int i=0;i<arrayFile.size();i++)
									{
										request = arrayFile.get(i);
										reqArray = request.split(" ");
										this.outputToClient.println(reqArray[1] + " " + reqArray[3]);
										this.outputToClient.flush();	
									}
									
									this.outputToClient.println("OK");
									this.outputToClient.flush();
								}
							}
							else if(respArray[0].equals("ADD"))
							{
								if(!respArray[1].isEmpty())
								{
									String storageFlie = "";
									//inetAddress = this.socket.getInetAddress();
									storageFlie +=  inetAddress.getHostAddress() + " ";
									for(int i=1;i<respArray.length;i++)
									{
										storageFlie += respArray[i]  + " ";
										
									}
									if(arrayFile.contains(storageFlie))
									{
										this.outputToClient.println("ERROR " + respArray[1] + "already exit");
										this.outputToClient.flush();
									}
									else
									{
										arrayFile.add(storageFlie);
										this.outputToClient.println("OK");
										this.outputToClient.flush();
									}
									
								}else
								{
									this.outputToClient.println("ERROR "  + "file name is empty");
									this.outputToClient.flush();
								}
								
								
							}
							else if(respArray[0].equals("DELETE"))
							{
								if(!respArray[1].isEmpty())
								{
									
									String storageFlie = "";
									//inetAddress = this.socket.getInetAddress();
									storageFlie +=  inetAddress.getHostAddress() + " ";
									for(int i=1;i<respArray.length;i++)
									{
										storageFlie += respArray[i]  + " ";
										
									}
									
									if(arrayFile.contains(storageFlie))
									{
										arrayFile.remove(storageFlie);
										this.outputToClient.println("OK");
										this.outputToClient.flush();
									}else
									{
										this.outputToClient.println("ERROR "  + "you don't share the " + respArray[1]);
										this.outputToClient.flush();
									}
									
									
								}else
								{
									this.outputToClient.println("ERROR "  + "file name is empty");
									this.outputToClient.flush();
								}
							}
							else if(respArray[0].equals("QUIT"))
							{
								this.outputToClient.println("GOODBYE");
								this.outputToClient.flush();
								
								this.inputFromClient.close();
								this.outputToClient.close();
								this.socket.close();
								break;//表示线程结束
							}else
							{
								this.outputToClient.println("ERROR " + "input is false");
								this.outputToClient.flush();
							}
							
					
					}	
				}catch(IOException e)
				{
					jta.append("Client " + inetAddress.getHostAddress() + " Port: "+ socket.getPort() + " exit server" +"\n");
					System.err.println(e);
				}
				
			
			
		}
		public BufferedReader getInputFromClient() {
			return inputFromClient;
		}
		public PrintWriter getOutputToClient() {
			return outputToClient;
		}
		public Socket getSocket()
		{
			return socket;
		}
		
	}
//	public static void main(String[] args)
//	{
//		Napd n = new Napd();
//	}
	
}
