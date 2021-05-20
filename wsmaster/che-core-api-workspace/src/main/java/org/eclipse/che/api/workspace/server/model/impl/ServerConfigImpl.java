/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.model.impl;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;

/** @author Alexander Garagatyi */
@Entity(name = "ServerConf")
@Table(name = "serverconf")
public class ServerConfigImpl implements ServerConfig {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "port")
  private String port;

  @Column(name = "protocol")
  private String protocol;

  @Column(name = "path")
  private String path;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "serverconf_attributes",
      joinColumns = @JoinColumn(name = "serverconf_id"))
  @MapKeyColumn(name = "attributes_key")
  @Column(name = "attributes")
  private Map<String, String> attributes;

  public ServerConfigImpl() {}

  public ServerConfigImpl(
      String port, String protocol, String path, Map<String, String> attributes) {
    this.port = port;
    this.protocol = protocol;
    this.path = path;
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
  }

  public ServerConfigImpl(ServerConfig serverConf) {
    this(
        serverConf.getPort(),
        serverConf.getProtocol(),
        serverConf.getPath(),
        serverConf.getAttributes());
  }

  @Override
  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public ServerConfigImpl withPort(String port) {
    this.port = port;
    return this;
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public ServerConfigImpl withProtocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  @Override
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ServerConfigImpl withPath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    } else {
      this.attributes = new HashMap<>();
    }
  }

  public ServerConfigImpl withAttributes(Map<String, String> attributes) {
    setAttributes(attributes);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServerConfigImpl)) {
      return false;
    }
    ServerConfigImpl that = (ServerConfigImpl) o;
    return Objects.equals(id, that.id)
        && Objects.equals(getPort(), that.getPort())
        && Objects.equals(getProtocol(), that.getProtocol())
        && Objects.equals(getPath(), that.getPath())
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, getPort(), getProtocol(), getPath(), getAttributes());
  }

  @Override
  public String toString() {
    return "ServerConfigImpl{"
        + "id="
        + id
        + ", port='"
        + port
        + '\''
        + ", protocol='"
        + protocol
        + '\''
        + ", path='"
        + path
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }

  public static ServerConfigImpl createFromEndpoint(Endpoint endpoint, boolean devfileEndpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());
    attributes.put(SERVER_NAME_ATTRIBUTE, endpoint.getName());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove(PUBLIC_ENDPOINT_ATTRIBUTE);
    if ("false".equals(isPublic)) {
      ServerConfig.setInternal(attributes, true);
    }

    ServerConfig.setRequireSubdomain(attributes, devfileEndpoint);

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }

  public static ServerConfigImpl createFromEndpoint(Endpoint endpoint) {
    return createFromEndpoint(endpoint, false);
  }
}
