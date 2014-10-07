package niosandbox;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import nio.engine.*;

public class TestNioServer extends NioServer {

	private int port;
	private ServerSocketChannel sc;

	public TestNioServer(int port, ServerSocketChannel sc) {
		this.port = port;
		this.sc = sc;
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return this.port;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {
			sc.close();
		} catch (IOException e) {
			// nothing to do, the channel is already closed
		}
	}
	

}
