/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ant.tools;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

/** @author andrew00x */
public class AntBuildListener implements BuildListener {
    private boolean autoFlush = true;

    private Socket             socket;
    private ObjectOutputStream out;
    private boolean            connect;

    public AntBuildListener() {
    }

    private void connect() {
        try {
            socket = new Socket("127.0.0.1", getPort());
            out = new ObjectOutputStream(socket.getOutputStream());
            connect = true;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void disconnect() {
        if (!connect) {
            return;
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        connect = false;
    }

    @Override
    public void buildStarted(BuildEvent event) {
        connect();
        send(new AntMessage(AntMessage.BUILD_STARTED, null, null, event.getMessage()));
    }

    @Override
    public void buildFinished(BuildEvent event) {
        final AntMessage message;
        final Throwable throwable = event.getException();
        if (throwable == null) {
            message = new AntMessage(AntMessage.BUILD_SUCCESSFUL, null, null, event.getMessage());
        } else {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            message = new AntMessage(AntMessage.BUILD_ERROR, null, null, writer.toString());
        }
        send(message);
        send(null);
        disconnect();
    }

    @Override
    public void targetStarted(BuildEvent event) {
    }

    @Override
    public void targetFinished(BuildEvent event) {
    }

    @Override
    public void taskStarted(BuildEvent event) {
    }

    @Override
    public void taskFinished(BuildEvent event) {
    }

    @Override
    public void messageLogged(BuildEvent event) {
        final Target target = event.getTarget();
        final Task task = event.getTask();
        final String text = event.getMessage();
        send(new AntMessage(AntMessage.BUILD_LOG,
                            target == null ? null : target.getName(),
                            task == null ? null : task.getTaskName(),
                            text)
            );
    }

    private void send(AntMessage message) {
        try {
            out.writeObject(message);
            if (autoFlush) {
                out.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private int getPort() {
        final String commandLine = System.getProperty("sun.java.command");
        final String myProperty = "-D" + getClass().getName() + ".port";
        String myPort = null;
        int start = commandLine.indexOf(myProperty);
        if (start > 0) {
            start = commandLine.indexOf('=', start);
            int end = commandLine.indexOf(' ', start);
            if (end < 0) {
                end = commandLine.length();
            }
            myPort = commandLine.substring(start + 1, end);
        }
        try {
            return Integer.parseInt(myPort);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable connect to the builder, connection port is not set.");
        }
    }
}
