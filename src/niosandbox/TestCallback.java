package niosandbox;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

import nio.engine.*;

/**
 * Call me class
 *
 */
public class TestCallback implements AcceptCallback, DeliverCallback,
		ConnectCallback {
	
	private TestEngine te;
	public TestCallback(TestEngine te) throws Exception{
		this.te=te;
	}

	@Override
	public void connected(NioChannel channel) {
		// TODO Auto-generated method stub
		System.out.println("My connection has arrived on " + channel.toString());
		try {
			channel.getChannel().register(te.getSelector(), SelectionKey.OP_WRITE);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Relative GET method on ByteBuffer reads one byte u retard... 
	 */
	@Override
	public void deliver(NioChannel channel, ByteBuffer bytes) {
		// TODO Auto-generated method stub
		System.out.println("Message delivered: "+ new String(bytes.array()));
	}

	@Override
	public void accepted(NioServer server, NioChannel channel) {
		// TODO Auto-generated method stub
		System.out.println("Connection accepted on the server port :<" + server.getPort() + "> on channel <" + channel.toString()+ ">");
		try {
			channel.getChannel().register(te.getSelector(), SelectionKey.OP_READ);
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void closed(NioChannel channel) {
		// TODO Auto-generated method stub
		System.out.println("Channel " + channel.toString() + " has been closed");
	}

}
