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
package org.eclipse.che.api.core.model.workspace.config;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Configuration of server that can be started inside of machine.
 *
 * @author Alexander Garagatyi
 */
public interface ServerConfig {

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can identify server as internal or
   * external. Attribute value {@code true} makes a server internal, any other value or lack of the
   * attribute makes the server external.
   */
  String INTERNAL_SERVER_ATTRIBUTE = "internal";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can identify server as secure or
   * non-secure. Requests to secure servers will be authenticated and must contain machine token.
   * Attribute value {@code true} makes a server secure, any other value or lack of the attribute
   * makes the server non-secure.
   */
  String SECURE_SERVER_ATTRIBUTE = "secure";

  /**
   * {@link ServerConfig} and {@link Server} attribute name which can contain an comma-separated
   * list of URI-s which are considered as non-secure on the given server and can be accessible with
   * unauthenticated requests.
   */
  String UNSECURED_PATHS_ATTRIBUTE = "unsecuredPaths";

  /**
   * Port used by server.
   *
   * <p>It may contain protocol(tcp or udp) after '/' symbol. If protocol is missing tcp will be
   * used by default. Example:
   *
   * <ul>
   *   <li>8080/tcp
   *   <li>8080/udp
   *   <li>8080
   * </ul>
   */
  String getPort();

  /**
   * Protocol for configuring preview url of this server.
   *
   * <p>Example:
   *
   * <ul>
   *   <li>http
   *   <li>https
   *   <li>tcp
   *   <li>udp
   *   <li>ws
   *   <li>wss
   * </ul>
   */
  String getProtocol();

  /** Path used by server. */
  @Nullable
  String getPath();

  /** Attributes of the server */
  Map<String, String> getAttributes();
}
