/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.connection;

import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.NOTIFICATION_CLOSE_SESSION;
import static org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.NOTIFICATION_CLOSE_MESSAGE_HANDLER;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.eclipse.che.plugin.zdb.server.ZendDebugger;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientMessage;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgClientMessages.IDbgClientResponse;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.CloseMessageHandlerNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineMessage;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineNotification;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineRequest;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgEngineMessages.IDbgEngineResponse;
import org.eclipse.che.plugin.zdb.server.exceptions.ZendDbgTimeoutException;

/**
 * The debug connection is responsible for initializing and handling a debug session that was
 * triggered by debugger engine.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgConnection {

  private EngineConnectionRunnable engineConnectionRunnable;
  private ExecutorService engineConnectionRunnableExecutor;
  private EngineMessageRunnable engineMessageRunnable;
  private ExecutorService engineMessageRunnableExecutor;
  private Map<Integer, EngineSyncResponse<IDbgEngineResponse>> engineSyncResponses =
      new HashMap<>();
  private final ZendDbgSettings debugSettings;
  private IEngineMessageHandler engineMessageHandler;
  private int debugRequestId = 1000;
  private boolean isConnected = false;

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
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream()); ) {
          socket.setReceiveBufferSize(1024 * 128);
          socket.setSendBufferSize(1024 * 128);
          socket.setTcpNoDelay(true);
          this.socket = socket;
          this.inputStream = inputStream;
          this.outputStream = outputStream;
          read();
        } catch (Exception e) {
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
          SSLServerSocket sslServerSocket =
              (SSLServerSocket)
                  SSLServerSocketFactory.getDefault()
                      .createServerSocket(debugSettings.getDebugPort());
          sslServerSocket.setEnabledCipherSuites(sslServerSocket.getSupportedCipherSuites());
          this.debugSocket = sslServerSocket;
        } else {
          this.debugSocket = new ServerSocket(debugSettings.getDebugPort());
        }
      } catch (Exception e) {
        ZendDebugger.LOG.error(e.getMessage(), e);
      }
    }

    private void read() {
      isConnected = true;
      while (true) {
        try {
          // Reads the message length
          int length = inputStream.readInt();
          if (length < 0) {
            ZendDebugger.LOG.error(
                "Zend debugger engine is probably using SSL, please check 'Use SSL encryption' option.");
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
            EngineSyncResponse<IDbgEngineResponse> syncResponse =
                engineSyncResponses.remove(response.getID());
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
        outputStream.writeInt(messageSize);
        byteArrayOutputStream.writeTo(outputStream);
        outputStream.flush();
      } catch (Exception e) {
        ZendDebugger.LOG.error(e.getMessage(), e);
      }
      if (clientMessage.getType() == NOTIFICATION_CLOSE_SESSION) {
        purge();
      }
    }

    private void purge() {
      if (socket != null && !socket.isClosed()) {
        try {
          socket.shutdownInput();
          socket.shutdownOutput();
        } catch (Exception e) {
          // ignore
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

    private BlockingQueue<IDbgEngineMessage> messageQueue =
        new ArrayBlockingQueue<IDbgEngineMessage>(100);

    @Override
    public void run() {
      while (true) {
        try {
          IDbgEngineMessage message = messageQueue.take();
          if (message.getType() == NOTIFICATION_CLOSE_MESSAGE_HANDLER) break;
          if (message instanceof IDbgEngineNotification) {
            engineMessageHandler.handleNotification((IDbgEngineNotification) message);
          } else if (message instanceof IDbgEngineRequest) {
            IDbgClientResponse response =
                engineMessageHandler.handleRequest((IDbgEngineRequest<?>) message);
            engineConnectionRunnable.write(response);
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

    private static final int TIMEOUT = 10;

    private final Semaphore semaphore = new Semaphore(0);;
    private T response;

    void set(T response) {
      this.response = response;
      semaphore.release();
    }

    T get() throws ZendDbgTimeoutException {
      boolean acquired = false;
      try {
        acquired = semaphore.tryAcquire(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        ZendDebugger.LOG.error(e.getMessage(), e);
      }
      if (acquired) {
        return this.response;
      }
      throw new ZendDbgTimeoutException(TIMEOUT, TimeUnit.SECONDS);
    }
  }

  public interface IEngineMessageHandler {

    void handleNotification(IDbgEngineNotification notification);

    <T extends IDbgClientResponse> T handleRequest(IDbgEngineRequest<T> request);
  }

  /**
   * Constructs a new DebugConnectionThread with a given Socket.
   *
   * @param socket
   */
  public ZendDbgConnection(
      IEngineMessageHandler engineMessageHandler, ZendDbgSettings debugSettings) {
    this.engineMessageHandler = engineMessageHandler;
    this.debugSettings = debugSettings;
  }

  /**
   * Start the connection with debugger. Causes connection & message handler threads to be started.
   */
  public void connect() {
    try {
      engineConnectionRunnableExecutor =
          Executors.newSingleThreadExecutor(
              new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                  final Thread thread =
                      new Thread(r, "ZendDbgClient:" + debugSettings.getDebugPort());
                  thread.setDaemon(true);
                  return thread;
                }
              });
      engineMessageRunnableExecutor =
          Executors.newSingleThreadExecutor(
              new ThreadFactory() {
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

  /** Closes the connection. Causes connection & message handler threads to be shutdown. */
  public void disconnect() {
    engineConnectionRunnable.close();
    engineConnectionRunnableExecutor.shutdown();
    engineMessageRunnableExecutor.shutdown();
  }

  /**
   * Sends given client request to Zend debugger engine.
   *
   * @param request
   * @return Zend debugger engine response
   */
  @SuppressWarnings("unchecked")
  public synchronized <T extends IDbgEngineResponse> T sendRequest(IDbgClientRequest<T> request) {
    if (!isConnected) {
      return null;
    }
    try {
      request.setID(debugRequestId++);
      EngineSyncResponse<T> syncResponse = new EngineSyncResponse<T>();
      engineSyncResponses.put(
          request.getID(), (EngineSyncResponse<IDbgEngineResponse>) syncResponse);
      engineConnectionRunnable.write(request);
      // Wait for response
      return syncResponse.get();
    } catch (ZendDbgTimeoutException e) {
      ZendDebugger.LOG.error("Could not get debugger engine response for " + request.toString(), e);
    } catch (Exception e) {
      ZendDebugger.LOG.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Sends given client notification to Zend debugger engine.
   *
   * @param request
   */
  public synchronized void sendNotification(IDbgClientNotification request) {
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
