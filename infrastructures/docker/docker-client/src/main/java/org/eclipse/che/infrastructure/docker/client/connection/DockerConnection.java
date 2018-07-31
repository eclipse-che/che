/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.connection;

import com.google.common.io.ByteStreams;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.commons.lang.Pair;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Mykola Morhun
 */
public abstract class DockerConnection implements Closeable {
  private String method;
  private String path;
  private Entity<?> entity;
  private StringBuilder query = new StringBuilder();
  private List<Pair<String, ?>> headers = new LinkedList<>();

  public DockerConnection method(String method) {
    this.method = method;
    return this;
  }

  public DockerConnection path(String path) {
    this.path = path;
    return this;
  }

  public DockerConnection query(String name, Object... values) {
    if (name == null) {
      throw new NullPointerException("Name is null");
    }
    if (values == null) {
      throw new NullPointerException("Values are null");
    }
    for (Object value : values) {
      if (value == null) {
        throw new NullPointerException("Value is null");
      }
      if (query.length() > 0) {
        query.append('&');
      }
      query.append(name).append('=').append(value.toString());
    }
    return this;
  }

  public DockerConnection header(String key, Object value) {
    this.headers.add(Pair.of(key, value));
    return this;
  }

  public DockerConnection headers(List<Pair<String, ?>> headers) {
    this.headers.addAll(headers);
    return this;
  }

  public DockerConnection entity(InputStream entity) {
    this.entity = new StreamEntity(entity);
    return this;
  }

  /** @deprecated use {@link #entity(byte[])} instead */
  @Deprecated
  public DockerConnection entity(String entity) {
    this.entity = new StringEntity(entity);
    return this;
  }

  public DockerConnection entity(byte[] entity) {
    this.entity = new BytesEntity(entity);
    return this;
  }

  public DockerResponse request() throws IOException {
    return request(method, path, query.toString(), headers, entity);
  }

  protected abstract DockerResponse request(
      String method, String path, String query, List<Pair<String, ?>> headers, Entity<?> entity)
      throws IOException;

  public abstract void close();

  abstract static class Entity<T> {
    final T entity;

    Entity(T entity) {
      this.entity = entity;
    }

    abstract void writeTo(OutputStream output) throws IOException;
  }

  static class StreamEntity extends Entity<InputStream> {
    StreamEntity(InputStream entity) {
      super(entity);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
      try {
        ByteStreams.copy(entity, output);
        output.flush();
      } finally {
        entity.close();
      }
    }
  }

  static class StringEntity extends Entity<String> {
    StringEntity(String entity) {
      super(entity);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
      output.write(entity.getBytes());
      output.flush();
    }
  }

  static class BytesEntity extends Entity<byte[]> {
    BytesEntity(byte[] entity) {
      super(entity);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
      output.write(entity);
      output.flush();
    }
  }
}
