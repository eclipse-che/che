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
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

import zend.com.che.plugin.zdb.server.ZendDebugger;

/**
 * The debug connection is responsible of initializing and handle a single debug
 * session that was triggered by a remote or local debugger.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugConnection {

	/**
	 * This job handles the requests and notification that are inserted into the
	 * queues by the message receiver job.
	 */
	private class MessageHandler extends Job {

		private BlockingQueue<IDebugMessage> messageQueue = new ArrayBlockingQueue<IDebugMessage>(100);

		public MessageHandler() {
			super("Debug Message Handler"); //$NON-NLS-1$
			setSystem(true);
			setUser(false);
			setPriority(LONG);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			while (true) {
				if (monitor.isCanceled())
					return Status.OK_STATUS;
				try {
					IDebugMessage message = messageQueue.take();
					try {
						debugSession.handle(message);
					} catch (Exception e) {
						ZendDebugger.LOG.error(e.getMessage(), e);
					}
				} catch (Exception e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
					shutdown();
				}
			}
		}

		public void queue(IDebugMessage m) {
			messageQueue.offer(m);
		}

		void shutdown() {
			cancel();
			messageQueue.clear();
		}

	}

	/**
	 * This job manages the communication initiated by the peer. All the
	 * messages that arrive from the peer are read by the receiver that will
	 * then handle the message by the message type.
	 */
	private class MessageReceiver extends Job {

		// Phantom message used to notify that connection was closed
		private final IDebugMessage INTERRUPT = new AbstractDebugMessage() {

			@Override
			public void deserialize(DataInputStream in) throws IOException {
			}

			@Override
			public int getType() {
				return 0;
			}

			@Override
			public void serialize(DataOutputStream out) throws IOException {
			}

		};

		public MessageReceiver() {
			super("Debug Message Receiver"); //$NON-NLS-1$
			setSystem(true);
			setUser(false);
			setPriority(LONG);
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			// 'while(true)' is OK here since we use blocking read
			while (true) {
				try {
					if (monitor.isCanceled())
						return Status.OK_STATUS;
					// Reads the length
					int length = connectionIn.readInt();
					if (length < 0) {
						String message = "Socket error (length is negative): possibly Server is SSL, Client is not."; //$NON-NLS-1$
						ZendDebugger.LOG.error(message);
						shutdown();
						continue;
					}
					// Message arrived. read its type identifier.
					int messageType = connectionIn.readShort();
					IDebugMessage message = ZendDebugMessageFactory.create(messageType);
					// Hard-coded encoding for now... (should be
					// preference/setting)
					message.setTransferEncoding(ENCODING);
					message.deserialize(connectionIn);
					if (message instanceof IDebugResponse) {
						IDebugResponse response = (IDebugResponse) message;
						if (syncQuery != null && syncQuery.request.getID() == response.getID()) {
							syncQuery.response = response;
							syncQuerySemaphore.release();
							// Continue, don't pass it to handler
							continue;
						}
					}
					messageHandler.queue(message);
				} catch (IOException e) {
					// Probably, the connection was dumped
					shutdown();
					continue;
				} catch (Exception e) {
					ZendDebugger.LOG.error(e.getMessage(), e);
					shutdown();
				}
			}
		}

		void shutdown() {
			cancel();
			// Shutdown message handler
			messageHandler.queue(INTERRUPT);
			// Have to be here as well as in message handler
			ZendDebugConnection.this.terminate();
		}

	}

	private static class SyncQuery {

		IDebugRequest request;
		IDebugResponse response;

	}

	public static final String ENCODING = "UTF-8";
	
	private ZendDebugSession debugSession;
	private Socket socket;
	private DataInputStream connectionIn;
	private DataOutputStream connectionOut;
	private MessageReceiver messageReceiver;
	private MessageHandler messageHandler;
	private boolean isConnected;
	private Semaphore syncQuerySemaphore = new Semaphore(0);
	private SyncQuery syncQuery;
	private int lastRequestId = 1000;

	/**
	 * Constructs a new DebugConnectionThread with a given Socket.
	 * 
	 * @param socket
	 */
	public ZendDebugConnection(ZendDebugSession debugSession, Socket socket) {
		this.debugSession = debugSession;
		this.socket = socket;
		connect();
	}

	public synchronized IDebugResponse syncRequest(IDebugRequest request) {
		if (!isConnected)
			// Skip if already disconnected
			return null;
		try {
			request.setID(lastRequestId++);
			syncQuery = new SyncQuery();
			syncQuery.request = request;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
			request.serialize(dataOutputStream);
			int messageSize = byteArrayOutputStream.size();
			// Write to connection output
			connectionOut.writeInt(messageSize);
			byteArrayOutputStream.writeTo(connectionOut);
			connectionOut.flush();
			// Wait for response
			syncQuerySemaphore.acquire();
			return syncQuery.response;
		} catch (Exception e) {
			// Return null for any exception
			String message = "Exception for request NO." + request.getType() //$NON-NLS-1$
					+ e.toString();
			ZendDebugger.LOG.error(message, e);
		}
		return null;
	}

	public synchronized void asyncRequest(IDebugRequest request) {
		if (!isConnected)
			// Skip if already disconnected
			return;
		try {
			request.setID(lastRequestId++);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
			request.serialize(dataOutputStream);
			int messageSize = byteArrayOutputStream.size();
			// Write to connection output
			connectionOut.writeInt(messageSize);
			byteArrayOutputStream.writeTo(connectionOut);
			connectionOut.flush();
		} catch (Exception e) {
			// Return null for any exception
			String message = "Exception for request NO." + request.getType() //$NON-NLS-1$
					+ e.toString();
			ZendDebugger.LOG.error(message, e);
		}
	}

	/**
	 * Closes the connection. Causes message receiver & handler to be shutdown.
	 */
	public synchronized void disconnect() {
		messageReceiver.shutdown();
	}

	/**
	 * Start the connection with debugger.
	 */
	private void connect() {
		try {
			socket.setTcpNoDelay(true);
			this.connectionIn = new DataInputStream(socket.getInputStream());
			this.connectionOut = new DataOutputStream(socket.getOutputStream());
			messageHandler = new MessageHandler();
			messageReceiver = new MessageReceiver();
			// Start message receiver
			messageReceiver.schedule();
			// Start message handler
			messageHandler.schedule();
			isConnected = true;
		} catch (Exception e) {
			ZendDebugger.LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Terminates connection completely.
	 */
	private void terminate() {
		if (!isConnected)
			return;
		// Mark it as closed already
		isConnected = false;
		// Clean socket
		if (socket != null) {
			try {
				socket.shutdownInput();
			} catch (Exception exc) {
				// ignore
			}
			try {
				socket.shutdownOutput();
			} catch (Exception exc) {
				// ignore
			}
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception exc) {
				// ignore
			} finally {
				socket = null;
			}
		}
		if (connectionIn != null) {
			try {
				connectionIn.close();
			} catch (Exception exc) {
				// ignore
			} finally {
				connectionIn = null;
			}
		}
		if (connectionOut != null) {
			try {
				connectionOut.close();
			} catch (Exception exc) {
				// ignore
			} finally {
				connectionOut = null;
			}
		}
	}

}
