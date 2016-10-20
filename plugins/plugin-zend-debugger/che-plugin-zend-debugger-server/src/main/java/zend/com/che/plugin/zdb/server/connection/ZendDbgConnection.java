/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.connection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import zend.com.che.plugin.zdb.server.ZendDebugger;
import zend.com.che.plugin.zdb.server.connection.ZendDbgEngineMessages.CloseMessageHandlerNotification;

import static zend.com.che.plugin.zdb.server.connection.ZendDbgEngineMessages.*;
import static zend.com.che.plugin.zdb.server.connection.ZendDbgClientMessages.*;

/**
 * The debug connection is responsible for initializing and handling a debug
 * session that was triggered by debugger engine.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgConnection {

	private final class EngineConnectionRunnable implements Runnable {

		private ServerSocket debugSocket;
		private Socket socket;
		private DataInputStream inputStream;
		private DataOutputStream outputStream;

		private EngineConnectionRunnable() {
			open();
		}

		@Override
		public void run() {
			while (!debugSocket.isClosed()) {
				try (Socket socket = debugSocket.accept();
						DataInputStream inputStream = new DataInputStream(socket.getInputStream());
						DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());) {
					socket.setReceiveBufferSize(1024 * 128);
					socket.setSendBufferSize(1024 * 128);
					socket.setTcpNoDelay(true);
					this.socket = socket;
					this.inputStream = inputStream;
					this.outputStream = outputStream;
					read();
				} catch (IOException e) {
					if (debugSocket.isClosed()) {
						break;
					}
					ZendDebugger.LOG.error(e.getMessage(), e);
				}
			}
			engineMessageRunnable.queue(new CloseMessageHandlerNotification());
		}

		private void open() {
			try {
				if (debugSettings.isUseSsslEncryption()) {
					SSLServerSocket sslServerSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault()
							.createServerSocket(debugSettings.getDebugPort());
					sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());
					this.debugSocket = sslServerSocket;
				} else {
					this.debugSocket = new ServerSocket(debugSettings.getDebugPort());
				}
			} catch (IOException e) {
				ZendDebugger.LOG.error(e.getMessage(), e);
			}
		}

		private void read() {
			isConnected = true;
			while (true) {
				try {
					// Reads the length
					int length = inputStream.readInt();
					if (length < 0) {
						ZendDebugger.LOG.error("Socket error: (length is negative)");
						purge();
						break;
					}
					// Engine message arrived. read its type identifier.
					int messageType = inputStream.readShort();
					IDbgEngineMessage engineMessage = ZendDbgEngineMessages.create(messageType);
					engineMessage.deserialize(inputStream);
					if (engineMessage instanceof IDbgEngineResponse) {
						// Engine response has arrived...
						IDbgEngineResponse response = (IDbgEngineResponse) engineMessage;
						EngineSyncResponse<IDbgEngineResponse> syncResponse = (EngineSyncResponse<IDbgEngineResponse>) engineSyncResponses
								.remove(response.getID());
						if (syncResponse != null) {
							// Release waiting request provider
							syncResponse.set(response);
							continue;
						}
					} else {
						// Notification or request from engine arrived...
						engineMessageRunnable.queue(engineMessage);
					}
				} catch (EOFException e) {
					// Engine closed the session
					break;
				} catch (Exception e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
					break;
				}
			}
			isConnected = false;
		}

		private void write(IDbgClientMessage clientMessage) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
			try {
				clientMessage.serialize(dataOutputStream);
				int messageSize = byteArrayOutputStream.size();
				// Write to connection output
				synchronized (this) {
					outputStream.writeInt(messageSize);
					byteArrayOutputStream.writeTo(outputStream);
					outputStream.flush();
				}
			} catch (IOException e) {
				ZendDebugger.LOG.error(e.getMessage(), e);
			}
			if (clientMessage.getType() == NOTIFICATION_CLOSE_SESSION) {
				purge();
			}
		}

		private void purge() {
			if (socket != null) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
				} catch (IOException e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
				}
			}
		}

		private void close() {
			try {
				purge();
				debugSocket.close();
			} catch (IOException e) {
				ZendDebugger.LOG.error(e.getMessage(), e);
			}
		}

	}

	private final class EngineMessageRunnable implements Runnable {

		private BlockingQueue<IDbgEngineMessage> messageQueue = new ArrayBlockingQueue<IDbgEngineMessage>(100);

		@Override
		public void run() {
			while (true) {
				try {
					IDbgEngineMessage message = messageQueue.take();
					if (message.getType() == NOTIFICATION_CLOSE_MESSAGE_HANDLER)
						break;
					try {
						if (message instanceof IDbgEngineNotification) {
							debugSession.handleNotification((IDbgEngineNotification) message);
						} else if (message instanceof IDbgEngineRequest) {
							IDbgClientResponse response = debugSession
									.handleRequest((IDbgEngineRequest<?>) message);
							engineConnectionRunnable.write(response);
						}
					} catch (Exception e) {
						ZendDebugger.LOG.error(e.getMessage(), e);
					}
				} catch (Exception e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
				}
			}
		}

		private void queue(IDbgEngineMessage m) {
			messageQueue.offer(m);
		}

	}

	private static final class EngineSyncResponse<T extends IDbgEngineResponse> {

		private final Semaphore semaphore = new Semaphore(0);;
		private T response;

		void set(T response) {
			this.response = response;
			semaphore.release();
		}

		T get() {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				ZendDebugger.LOG.error(e.getMessage(), e);
			}
			return this.response;
		}

	}

	interface IEngineMessageHandler {

		void handleNotification(IDbgEngineNotification notification);

		<T extends IDbgClientResponse> T handleRequest(IDbgEngineRequest<T> request);

	}

	private EngineConnectionRunnable engineConnectionRunnable;
	private ExecutorService engineConnectionRunnableExecutor;
	private EngineMessageRunnable engineMessageRunnable;
	private ExecutorService engineMessageRunnableExecutor;
	private Map<Integer, EngineSyncResponse<IDbgEngineResponse>> engineSyncResponses = new HashMap<>();
	private final ZendDbgSessionSettings debugSettings;
	private ZendDbgSession debugSession;
	private int debugRequestId = 1000;
	private boolean isConnected = false;

	/**
	 * Constructs a new DebugConnectionThread with a given Socket.
	 * 
	 * @param socket
	 */
	public ZendDbgConnection(ZendDbgSession debugSession, ZendDbgSessionSettings debugSettings) {
		this.debugSession = debugSession;
		this.debugSettings = debugSettings;
	}

	/**
	 * Start the connection with debugger.
	 */
	void connect() {
		try {
			engineConnectionRunnableExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, "ZendDbgClient:" + debugSettings.getDebugPort());
					thread.setDaemon(true);
					return thread;
				}
			});
			engineMessageRunnableExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, "ZendDbgMsgHandler");
					thread.setDaemon(true);
					return thread;
				}
			});
			engineConnectionRunnable = new EngineConnectionRunnable();
			engineMessageRunnable = new EngineMessageRunnable();
			engineConnectionRunnableExecutor.execute(engineConnectionRunnable);
			engineMessageRunnableExecutor.execute(engineMessageRunnable);
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Closes the connection. Causes message receiver & handler to be shutdown.
	 */
	void disconnect() {
		engineConnectionRunnable.close();
		engineConnectionRunnableExecutor.shutdown();
		engineMessageRunnableExecutor.shutdown();
	}

	@SuppressWarnings("unchecked")
	public <T extends IDbgEngineResponse> T sendRequest(IDbgClientRequest<T> request) {
		if (!isConnected) {
			return null;
		}
		try {
			request.setID(debugRequestId++);
			EngineSyncResponse<T> syncResponse = new EngineSyncResponse<T>();
			engineSyncResponses.put(request.getID(), (EngineSyncResponse<IDbgEngineResponse>) syncResponse);
			engineConnectionRunnable.write(request);
			// Wait for response
			return syncResponse.get();
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public void sendNotification(IDbgClientNotification request) {
		if (!isConnected) {
			return;
		}
		try {
			engineConnectionRunnable.write(request);
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
	}

}
