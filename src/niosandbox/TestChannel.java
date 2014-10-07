package niosandbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import nio.engine.*;

/**
 * we dont want clients to see socket channel
 * 
 * @author pingu
 *
 */
public class TestChannel extends NioChannel {

	SocketChannel engineChannel;
	ByteBuffer bb;

	public TestChannel(SocketChannel sc) {
		try {
			this.engineChannel = sc;
			this.engineChannel.configureBlocking(false);
			bb = ByteBuffer.allocate(512);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public SocketChannel getChannel() {
		// TODO Auto-generated method stub
		return this.engineChannel;
	}

	@Override
	public void setDeliverCallback(DeliverCallback callback) {
		// TODO Auto-generated method stub
		callback.deliver(this, this.bb);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		// TODO Auto-generated method stub
		try {
			return (InetSocketAddress) this.engineChannel.getRemoteAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void send(ByteBuffer buf) {
		// TODO Auto-generated method stub
		this.bb = buf;
		try {
			this.engineChannel.write(this.bb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send(byte[] bytes, int offset, int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {
			this.engineChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
