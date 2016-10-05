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

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import zend.com.che.plugin.zdb.server.ZendDebugger;

/**
 * Abstract debugger daemon thread.
 * 
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractDebugDaemon {

	/**
	 * The thread responsible of listening for debug requests.
	 */
	private class ReceiverThread implements Runnable {
		@Override
		public void run() {
			isInitialized = true;
			try {
				while (isAlive) {
					Socket socket = serverSocket.accept();
					socket.setReceiveBufferSize(1024 * 128);
					socket.setSendBufferSize(1024 * 128);
					startConnection(socket);
				}
			} catch (IOException e) {
				synchronized (lock) {
					if (isAlive) {
						ZendDebugger.LOG.error(
								"Error while listening to incoming debug requests. Listen thread terminated!", e);
						isAlive = false;
					}
				}
			}
		}
	}

	protected final Object lock = new Object();
	protected ServerSocket serverSocket;
	protected boolean isAlive;
	protected Thread listenerThread;
	private boolean isInitialized;

	public void handleMultipleBindingError() {
		final int port = getReceiverPort();
		ZendDebugger.LOG
				.error("The debug port " + port + " is in use. Please select a different port for the debugger.");
	}

	public void init() {
		resetSocket();
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isInitialized() {
		synchronized (lock) {
			return isInitialized;
		}
	}

	public boolean isListening(int port) {
		synchronized (lock) {
			return isAlive && getReceiverPort() == port;
		}
	}

	public boolean resetSocket() {
		stopListen();
		int port = getReceiverPort();
		try {
			synchronized (lock) {
				serverSocket = new ServerSocket(port);
				startListen();
				return true;
			}
		} catch (BindException exc) {
			handleMultipleBindingError();
		} catch (IOException e) {
			ZendDebugger.LOG.error("Error while restting the socket for the debug requests.", e);
		}
		return false;
	}

	public void startListen() {
		synchronized (lock) {
			if (!isAlive && serverSocket != null) {
				startListenThread();
			} else {
				isInitialized = true;
			}
		}
	}

	public void stopListen() {
		synchronized (lock) {
			isAlive = false;
			if (serverSocket != null) {
				try {
					if (!serverSocket.isClosed()) {
						serverSocket.close();
					}
				} catch (SocketException se) {
					// do nothing in this case
				} catch (IOException e) {
					ZendDebugger.LOG.error("Problem while closing the debugger ServerSocket.", e);
				} finally {
					serverSocket = null;
				}
			}
		}
		try {
			// Wait for the listener thread to die.
			// Wait, at most, 2 seconds.
			if (listenerThread != null) {
				listenerThread.join(2000);
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Returns the server socket port used for the debug requests listening
	 * thread.
	 * 
	 * @return The port specified in the preferences.
	 */
	public abstract int getReceiverPort();

	/**
	 * Returns the debugger ID that is using this communication daemon.
	 * 
	 * @return The debugger ID that is using this daemon.
	 */
	public abstract String getDebuggerID();

	/**
	 * Starts a connection on the given Socket. This method should be overridden
	 * by extending classes to create a different debug connections.
	 * 
	 * @param socket
	 */
	protected abstract void startConnection(Socket socket);

	protected void startListenThread() {
		synchronized (lock) {
			if (isAlive) {
				return;
			}
			isAlive = true;
		}
		String port = " - Port: " //$NON-NLS-1$
				+ ((serverSocket != null) ? String.valueOf(serverSocket.getLocalPort()) : "??"); //$NON-NLS-1$
		listenerThread = new Thread(new ReceiverThread(), "PHP Debugger Daemon Thread " + port); //$NON-NLS-1$
		listenerThread.setDaemon(true);
		listenerThread.start();
	}
	
}
