package com.dragon.client;

import com.dragon.server.Server;

public class Test {
	public static void main(String[] args) {
		Server server = new Server(20000);
		server.work();
	}
}

