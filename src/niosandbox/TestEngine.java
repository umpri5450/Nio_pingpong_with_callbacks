package niosandbox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import nio.engine.*;

/**
 * when a client disconnects (CTRL+C or crash) we need to update the list of
 * 
 * this is an eventNotifier that is waiting for events clients
 */
public class TestEngine extends NioEngine implements Runnable {

	// WHO AM I??
	private boolean isServer;

	private Selector selector;

	// this is only for accepting connection purposes
	private ServerSocketChannel ssc;
	// private int port; now in TestNioServer
	private TestNioServer serv;

	// for firing-events
	private TestCallback callback;

	// buffers for writing data (1 buffer for read/write per channel)
	private Hashtable<TestChannel, ByteBuffer> myBuffers;

	// static messages for clients and servers
	public static String clientMsg = "From client with love";
	public static String serverMsg = "From server with love";

	public TestEngine(int port) throws Exception {
		super();
		// TODO Auto-generated constructor stub

		this.myBuffers = new Hashtable<TestChannel, ByteBuffer>();
		this.selector = Selector.open();
		this.ssc = ServerSocketChannel.open();

		// can't we change NioEngine for localhost to be protected?
		InetAddress host = InetAddress.getByName("localhost");
		InetSocketAddress isa = new InetSocketAddress(host, port);
		this.ssc.socket().bind(isa);
		this.ssc.configureBlocking(false);

		this.serv = new TestNioServer(port, this.ssc);

		this.callback = new TestCallback(this);
		this.isServer = false;
	}

	public Selector getSelector() {
		return this.selector;
	}

	@Override
	public void mainloop() {
		// TODO Auto-generated method stub
		System.out.println("TestEngine running");
		while (true) {
			try {

				selector.select();

				Iterator<?> selectedKeys = this.selector.selectedKeys()
						.iterator();

				while (selectedKeys.hasNext()) {

					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					/*
					 * Im using 1 channel for connection and 1 channel for
					 * exchanging messages
					 */
					if (!key.isValid()) {
						continue;

					} else if (key.isAcceptable()) {
						ServerSocketChannel acceptSSC = (ServerSocketChannel) key
								.channel();
						SocketChannel acceptSC = acceptSSC.accept();

						// this is resetted each time a socketchannel changes
						acceptSC.configureBlocking(false);

						TestChannel tc = new TestChannel(acceptSC);

						// save this channel into Hashtable with a new
						// ByteBuffer
						this.myBuffers.put(tc, ByteBuffer.allocate(512));

						this.callback.accepted(this.serv, tc);

					} else if (key.isReadable()) {

						SocketChannel readSC = (SocketChannel) key.channel();
						TestChannel omg = this.getExistingTC(readSC);
						
						//Get existing buffer from this channel 
						ByteBuffer readBuf = this.myBuffers.get(omg);
						
						//clears the buffer before reading into it
						readBuf.clear();
						readSC.read(readBuf);

						this.callback.deliver(omg, readBuf);
						readSC.register(this.selector, SelectionKey.OP_WRITE);

					} else if (key.isWritable()) {
						try {
							SocketChannel writeSC = (SocketChannel) key
									.channel();
							TestChannel omg = this.getExistingTC(writeSC);

							ByteBuffer writeBuf = this.myBuffers.get(omg);
							if (this.isServer) {
								writeBuf = ByteBuffer
										.wrap(serverMsg.getBytes());
							} else {
								writeBuf = ByteBuffer
										.wrap(clientMsg.getBytes());
							}
							omg.send(writeBuf);

							writeSC.register(this.selector,
									SelectionKey.OP_READ);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (key.isConnectable()) {
						SocketChannel connectSC = (SocketChannel) key.channel();
						connectSC.configureBlocking(false);

						while (!connectSC.finishConnect()) {
							System.out
									.println("Client : Waiting to finish establishing connection");
						}

						TestChannel tc = new TestChannel(connectSC);
						this.myBuffers.put(tc, ByteBuffer.allocate(512));

						this.callback.connected(tc);

					} else
						System.out.println("  ---> unknow key=");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public NioServer listen(int port, AcceptCallback callback)
			throws IOException {
		// TODO Auto-generated method stub
		this.ssc.configureBlocking(false);
		this.ssc.register(this.selector, SelectionKey.OP_ACCEPT);
		return this.serv;
	}

	@Override
	public void connect(InetAddress hostAddress, int port,
			ConnectCallback callback) throws UnknownHostException,
			SecurityException, IOException {
		// TODO Auto-generated method stub
		SocketChannel connectSC = SocketChannel.open();
		connectSC.configureBlocking(false);
		connectSC.register(this.selector, SelectionKey.OP_CONNECT);
		connectSC.connect(new InetSocketAddress(hostAddress, port));
	}

	public static void main(String argc[]) throws Exception {
		int myPort = Integer.parseInt(argc[0]);

		try {
			TestEngine me = new TestEngine(myPort);
			new Thread(me).start();
			if (argc[1].equals("client")) {
				me.connect(InetAddress.getByName("localhost"), 11111,
						me.callback);
			} else {
				me.listen(me.serv.getPort(), me.callback);
				me.setProfile(true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.mainloop();
	}

	public void setProfile(boolean server) {
		if (server) {
			this.isServer = true;
		} else {
			this.isServer = false;
		}
	}

	public TestChannel getExistingTC(SocketChannel test) throws Exception {
		Map<TestChannel, ByteBuffer> map = this.myBuffers;
		Iterator<Map.Entry<TestChannel, ByteBuffer>> ite = map.entrySet()
				.iterator();
		SocketChannel sctest;
		while (ite.hasNext()) {
			Map.Entry<TestChannel, ByteBuffer> entry = ite.next();
			sctest = entry.getKey().getChannel();
			if (sctest.equals(test)) {
				return entry.getKey();
			}
		}
		throw new Exception("Cette connection n'existe pas");
	}
}
