package com.dragon.app;

import java.io.IOException;
import java.io.Writer;

import com.api.web.Request;
import com.api.web.Response;
import com.api.web.Servlet;

public class MyServlet implements Servlet {

	@Override
	public void service(Request request, Response response) {
		StringBuilder sb = new StringBuilder();
		response.setProtocol("http/1.1");
		response.setStatusCode(200);
		response.setStatusValue("ok");
		response.setContentEncoding("utf-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setConnection("keep-alive");
		sb.append("<!Doctype html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<title>test</title>\n" + 
				"<meta content=\"text/html;charset=utf-8\">\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<font color=\"green\">welcome</font>\n" + 
				"</body>\n" + 
				"</html>");
		String body = sb.toString();
		response.setBody(body);
		Writer wr = response.getWriter();
		try {
			wr.write("");
			wr.flush();
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

