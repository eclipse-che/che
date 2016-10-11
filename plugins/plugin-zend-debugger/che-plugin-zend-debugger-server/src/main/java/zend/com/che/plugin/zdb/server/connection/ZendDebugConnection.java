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

import zend.com.che.plugin.zdb.server.ZendDebugger;
import zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.CloseMessageHandlerNotification;

import static zend.com.che.plugin.zdb.server.connection.ZendEngineMessages.*;

/**
 * The debug connection is responsible for initializing and handling a debug
 * session that was triggered by debugger engine.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugConnection {

	private final class EngineConnectionRunnable implements Runnable {

		private DataInputStream inputStream;
		private DataOutputStream outputStream;

		@Override
		public void run() {
			while (!debugSocket.isClosed()) {
				try (Socket socket = debugSocket.accept();
						DataInputStream inputStream = new DataInputStream(socket.getInputStream());
						DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());) {
					socket.setReceiveBufferSize(1024 * 128);
					socket.setSendBufferSize(1024 * 128);
					socket.setTcpNoDelay(true);
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
			engineNotificationRunnable.queue(new CloseMessageHandlerNotification());
		}

		private void read() {
			isConnected = true;
			while (true) {
				try {
					// Reads the length
					int length = inputStream.readInt();
					if (length < 0) {
						ZendDebugger.LOG.error("Socket error: (length is negative)");
						break;
					}
					// Engine message arrived. read its type identifier.
					int messageType = inputStream.readShort();
					IDebugEngineMessage engineMessage = ZendEngineMessages.create(messageType);
					engineMessage.deserialize(inputStream);
					if (engineMessage instanceof IDebugEngineNotification) {
						engineNotificationRunnable.queue((IDebugEngineNotification) engineMessage);
					} else if (engineMessage instanceof IDebugEngineResponse) {
						IDebugEngineResponse response = (IDebugEngineResponse) engineMessage;
						EngineSyncResponse<IDebugEngineResponse> syncResponse = (EngineSyncResponse<IDebugEngineResponse>) engineSyncResponses
								.remove(response.getID());
						if (syncResponse != null) {
							// Set response and release waiting request provider
							syncResponse.set(response);
							continue;
						}
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

		private void write(IDebugClientMessage clientMessage) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
			try {
				clientMessage.serialize(dataOutputStream);
				int messageSize = byteArrayOutputStream.size();
				// Write to connection output
				outputStream.writeInt(messageSize);
				byteArrayOutputStream.writeTo(outputStream);
				outputStream.flush();
			} catch (IOException e) {
				ZendDebugger.LOG.error(e.getMessage(), e);
			}
		}

	}

	private static final class EngineNotificationRunnable implements Runnable {

		private IEngineNotificationHandler handler;
		private BlockingQueue<IDebugEngineNotification> messageQueue = new ArrayBlockingQueue<IDebugEngineNotification>(
				100);

		private EngineNotificationRunnable(IEngineNotificationHandler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			while (true) {
				try {
					IDebugEngineNotification message = messageQueue.take();
					if (message.getType() == NOTIFICATION_CLOSE_MESSAGE_HANDLER)
						break;
					try {
						handler.handle(message);
					} catch (Exception e) {
						ZendDebugger.LOG.error(e.getMessage(), e);
					}
				} catch (Exception e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
				}
			}
		}

		private void queue(IDebugEngineNotification m) {
			messageQueue.offer(m);
		}

	}

	private static final class EngineSyncResponse<T extends IDebugEngineResponse> {

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
				// TODO
			}
			return this.response;
		}

	}

	interface IEngineNotificationHandler {

		void handle(IDebugEngineNotification notification);

	}

	private EngineConnectionRunnable engineConnectionRunnable;
	private ExecutorService engineConnectionRunnableExecutor;
	private EngineNotificationRunnable engineNotificationRunnable;
	private ExecutorService engineNotificationRunnableExecutor;
	private Map<Integer, EngineSyncResponse<IDebugEngineResponse>> engineSyncResponses = new HashMap<>();
	private int debugPort;
	private ZendDebugSession debugSession;
	private ServerSocket debugSocket;
	private int debugRequestId = 1000;
	private boolean isConnected = false;

	/**
	 * Constructs a new DebugConnectionThread with a given Socket.
	 * 
	 * @param socket
	 */
	public ZendDebugConnection(ZendDebugSession debugSession, int debugPort) {
		this.debugSession = debugSession;
		this.debugPort = debugPort;
	}

	/**
	 * Start the connection with debugger.
	 */
	void connect() {
		try {
			this.debugSocket = new ServerSocket(debugPort);
			engineConnectionRunnableExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, "ZendDbgClient:" + debugPort);
					thread.setDaemon(true);
					return thread;
				}
			});
			engineNotificationRunnableExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, "ZendDbgMsgHandler");
					thread.setDaemon(true);
					return thread;
				}
			});
			engineConnectionRunnable = new EngineConnectionRunnable();
			engineNotificationRunnable = new EngineNotificationRunnable(debugSession);
			engineConnectionRunnableExecutor.execute(engineConnectionRunnable);
			engineNotificationRunnableExecutor.execute(engineNotificationRunnable);
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Closes the connection. Causes message receiver & handler to be shutdown.
	 */
	void disconnect() {
		if (debugSocket != null) {
			try {
				debugSocket.close();
			} catch (IOException e) {
				// ignore
			}
		}
		engineConnectionRunnableExecutor.shutdown();
		engineNotificationRunnableExecutor.shutdown();
	}

	@SuppressWarnings("unchecked")
	public synchronized <T extends IDebugEngineResponse> T sendRequest(IDebugClientRequest<T> request) {
		if (!isConnected) {
			return null;
		}
		try {
			request.setID(debugRequestId++);
			EngineSyncResponse<T> syncResponse = new EngineSyncResponse<T>();
			engineSyncResponses.put(request.getID(), (EngineSyncResponse<IDebugEngineResponse>) syncResponse);
			engineConnectionRunnable.write(request);
			// Wait for response
			return syncResponse.get();
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public synchronized void sendNotification(IDebugClientNotification request) {
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
