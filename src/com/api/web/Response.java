package com.api.web;

import java.io.Writer;

public interface Response {
	void setProtocol(String protocol);
	void setStatusCode(int statusCode);
	void setStatusValue(String statusValue);
	void setConnection(String connectionValue);
	void setContentEncoding(String contentEncoding);
	void setContentType(String contentType);
	void setBody(String body);
	Writer getWriter();
}
