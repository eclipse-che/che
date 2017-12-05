/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.config;

import java.util.Map;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Configuration of server that can be started inside of machine.
 *
 * @author Alexander Garagatyi
 */
public interface ServerConfig {

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
