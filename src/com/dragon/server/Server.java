package com.dragon.server;

import com.api.web.*;
import com.dragon.app.MyServlet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	private int status = 0;
	private ServerSocket serverSocket;
	private Map<Socket, Runnable> map = new ConcurrentHashMap<Socket, Runnable>();

	public Server(int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<Socket, Runnable> getMap() {
		return map;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public int getStatus() {
		return status;
	}

	public void setMap(Map<Socket, Runnable> map) {
		this.map = map;
	}
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void work() {
		status = 1;
		while (status == 1) {
			try {
				Socket socket = serverSocket.accept();
				if (map.containsKey(socket))
					continue;
				Runnable r = new RequestHandler(this, socket);
				map.put(socket, r);
				new Thread(r).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		this.status = 0;
	}
}


class RequestHandler implements Runnable {
	private Server server;
	private Socket clientSocket;
	private Request request;
	
	public RequestHandler(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
	}
	
	public Socket getClietntSocket() {
		return clientSocket;
	}
	public Request handleNet(InputStream in) {
		BufferedReader reader = null;
		String msg = "";
		final Map<String, Object> map = new HashMap<>();
		Map<String, String> headers = new HashMap<String, String>();
		map.put("headers", headers);
		request = new SimpleRequest(map);
		new Thread(new Runnable() {

			@Override
			public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(request.getMethod());
					Map<String, String> headInfos = request.getHeaders();
					for (Map.Entry entry : headInfos.entrySet()) {
						System.out.println(entry.getKey() + ": " + entry.getValue());	
					}
					System.out.println(request.getParamMap());
					Request request = new SimpleRequest(map);
					Response response = null;
					try {
						response = new SimpleResponse(new OutputStreamWriter(clientSocket.getOutputStream(), "utf-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Servlet servlet = new MyServlet();
					servlet.service(request, response);
					Writer wr = null;
					try {
						wr = response.getWriter();
						if (wr != null)
							wr.write("");
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (wr != null)
							try {
								wr.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
					exit();
			}
			
		}).start();
		try {
			reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
			String tmp = null;
			while ((tmp = reader.readLine()) != null) {
				msg += tmp + "\n";
				String[] strs = null;
				if (tmp.contains(":")) {
					strs = tmp.split(":");
					if (strs.length == 2)
						headers.put(strs[0], strs[1]);
				} else if (tmp.contains(" ")) {
					strs = tmp.split(" ");
					if (strs.length == 3) {
						map.put("method", strs[0]);
						map.put("protocol", strs[2]);
						if (strs[1].contains("?")) {
							strs = strs[1].split("[?]");
							map.put("uri", strs[0]);
							if (strs.length == 2) {
								if (strs[1].contains("&"))
									strs = strs[1].split("[&]");
								else 
									strs = new String[] {strs[1]};
								Map<String, List<String>> params = new HashMap<>();
								map.put("params", params);
								String[] pnv = null;
								for (String s : strs) {
									pnv = s.split("=");
									if (!params.containsKey(pnv[0]))
										params.put(pnv[0], new ArrayList<String>());
									params.get(pnv[0]).add(pnv[1]);
								}
							}
						}
					}	
				}
			}
			System.out.println(msg);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return request;
	}
	public void write(String msg) {
		OutputStream out = null;
		BufferedWriter writer = null;
		try {
			out = clientSocket.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
			writer.write(msg);
			writer.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (out != null)
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	@Override
	public void run() {
		try {
			handleNet(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setClietntSocket(Socket clietntSocket) {
		this.clientSocket = clietntSocket;
	}
	
	public void exit() {
		System.out.println("关闭: " + Thread.currentThread().getName());
		System.out.println("Thread count: " + server.getMap().size());
		server.getMap().remove(clientSocket);
		System.out.println("Thread count: " + server.getMap().size());
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientSocket = null;
	}
}

class SimpleRequest implements Request {
	private Map<String, Object> innerMap;
	public SimpleRequest() {}
	public SimpleRequest(Map<String, Object> map) {
		this.innerMap = map;
	}
	@Override
	public String getMethod() {
		return (String)innerMap.get("method");
	}
	@Override
	public Map<String, String> getHeaders() {
		return (Map<String, String>)innerMap.get("headers");
	}
	@Override
	public List<String> getParams(String paramName) {
		// TODO Auto-generated method stub
		return (List<String>)((Map<String, Object>)innerMap.get("params")).get(paramName);
	}
	@Override
	public Map<String, List<String>> getParamMap() {
		// TODO Auto-generated method stub
		return (Map<String, List<String>>)innerMap.get("params");
	}	
}

class SimpleResponse implements Response {
	private boolean unWriten = true;
	private Map<String, String> map = new HashMap<>();
	private StringBuilder sb = new StringBuilder();
	private Writer writer;
	public SimpleResponse(Writer writer) {
		this.writer = new Writer() {
			private Writer innerWriter = writer; 
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				if (!unWriten)
					return;
				String str = new String(cbuf, off, len);
				sb.append(map.get("protocol")).append(" ").append(map.get("statusCode")).append(" ").append(map.get("statusValue")).append("\n");
				sb.append("Connection:").append(map.get("connectionValue")).append("\n");
				sb.append("Content-Encoding:").append(map.get("contentEncoding")).append("\n");
				sb.append("Content-Type:").append(map.get("contentType")).append("\n\n");
				if (map.get("body") != null)
					sb.append(map.get("body"));
				sb.append(str);
				innerWriter.write(sb.toString());
			}

			@Override
			public void flush() throws IOException {
				innerWriter.flush();
			}

			@Override
			public void close() throws IOException {
				innerWriter.close();
			}
			public void Write(String str) {
				if (!unWriten)
					return;
				sb.append(map.get("protocol")).append(" ").append(map.get("statusCode")).append(" ").append(map.get("statusValue")).append("\n");
				sb.append("Connection:").append(map.get("connectionValue")).append("\n");
				sb.append("Content-Encoding:").append(map.get("contentEncoding")).append("\n");
				sb.append("Content-Type:").append(map.get("contentType")).append("\n\n");
				if (map.get("body") != null)
					sb.append(map.get("body"));
				sb.append(str);
				str = sb.append(str).toString();
				try {
					innerWriter.write(str);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
			
		};
	}
	@Override
	public void setProtocol(String protocol) {
		map.put("protocol", protocol);
	}

	@Override
	public void setStatusCode(int statusCode) {
		map.put("statusCode", statusCode + "");
	}

	@Override
	public void setStatusValue(String statusValue) {
		map.put("statusValue", statusValue);
	}

	@Override
	public void setConnection(String connectionValue) {
		map.put("connectionValue", connectionValue);
	}

	@Override
	public void setContentEncoding(String contentEncoding) {
		map.put("contentEncoding", contentEncoding);
	}
	
	@Override
	public void setContentType(String contentType) {
		map.put("contentType", contentType);
		
	}
	@Override
	public void setBody(String body) {
		map.put("body", body);	
	}
	@Override
	public Writer getWriter() {
		return this.writer;
	}	
}