/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace.runtime;

import java.util.Map;

/**
 * Server Runtime exposed by URL
 *
 * @author gazarenkov
 */
public interface Server {

  /** @return URL exposing the server */
  String getUrl();

  /** @return the status */
  ServerStatus getStatus();

  /**
   * Returns attributes of the server with some metadata. You can use static methods on {@link
   * org.eclipse.che.api.core.model.workspace.config.ServerConfig} to evaluate attributes in this
   * map easily.
   */
  Map<String, String> getAttributes();
}
