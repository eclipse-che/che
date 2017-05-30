/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client.connection;

import com.google.common.base.Strings;

import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.plugin.docker.client.CLibrary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.util.List;

import static org.eclipse.che.plugin.docker.client.CLibrary.AF_UNIX;
import static org.eclipse.che.plugin.docker.client.CLibrary.SOCK_STREAM;
import static org.eclipse.che.plugin.docker.client.CLibrary.SockAddrUn;
import static org.eclipse.che.plugin.docker.client.CLibraryFactory.getCLibrary;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class UnixSocketConnection extends DockerConnection {
    private final String dockerSocketPath;

    private int fd = -1;

    public UnixSocketConnection(String dockerSocketPath) {
        this.dockerSocketPath = dockerSocketPath;
    }

    @Override
    protected DockerResponse request(String method, String path, String query, List<Pair<String, ?>> headers, Entity<?> entity)
            throws IOException {
        fd = connect();
        final OutputStream output = new BufferedOutputStream(openOutputStream(fd));
        writeHttpHeaders(output, method, path, query, headers);
        if (entity != null) {
            entity.writeTo(output);
        }
        return new UnixSocketDockerResponse(new BufferedInputStream(openInputStream(fd)));
    }


    @Override
    public void close() {
        if (fd != -1) {
            getCLibrary().close(fd);
        }
    }

    private int connect() throws IOException {
        final CLibrary cLib = getCLibrary();
        int fd = cLib.socket(AF_UNIX, SOCK_STREAM, 0);
        if (fd == -1) {
            throw new ConnectException(String.format("Unable connect to unix socket: '%s'", dockerSocketPath));
        }
        final SockAddrUn sockAddr = new SockAddrUn(dockerSocketPath);
        int c = cLib.connect(fd, sockAddr, sockAddr.size());
        if (c == -1) {
            throw new ConnectException(String.format("Unable connect to unix socket: '%s'", dockerSocketPath));
        }
        return fd;
    }

    private void writeHttpHeaders(OutputStream output, String method, String path, String query, List<Pair<String, ?>> headers)
            throws IOException {
        final Writer writer = new OutputStreamWriter(output);
        writer.write(method);
        writer.write(' ');
        writer.write(path);
        if (!Strings.isNullOrEmpty(query)) {
            writer.write("?");
            writer.write(query);
        }
        writer.write(" HTTP/1.1\r\n");
        for (Pair<String, ?> header : headers) {
            writer.write(header.first);
            writer.write(": ");
            writer.write(String.valueOf(header.second));
            writer.write("\r\n");
        }
        // Host header is mandatory in HTTP 1.1
        writer.write("Host: \r\n\r\n");
        writer.flush();
    }

    private InputStream openInputStream(int fd) {
        return new UnixSocketInputStream(fd);
    }

    private OutputStream openOutputStream(int fd) {
        return new UnixSocketOutputStream(fd);
    }
}
