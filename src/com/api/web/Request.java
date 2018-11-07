package com.api.web;

import java.util.List;
import java.util.Map;

public interface Request {
	String getMethod();
	Map<String, String> getHeaders();
	List<String> getParams(String paramName);
	Map<String, List<String>> getParamMap();
}
