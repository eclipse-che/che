/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.urlfactory;

import java.util.Map;

/**
 * Provides basic information about the remote factory URL components. Vendor specific
 * implementations may provide wider range of details about the URL (like username, repository etc).
 */
public interface RemoteFactoryUrl {

  /**
   * Filename of factory json (typically .factory.json) or {@code null} if it cannot be clearly
   * detected from provided factory url.
   *
   * @return factory json filename
   */
  String getFactoryFilename();

  /**
   * Location of factory json. Must points to the raw factory file content.
   *
   * @return factory json file location
   */
  String factoryFileLocation();

  /**
   * Possible locations of devfile. Must points to the raw devfile content.
   *
   * @return devfile file locations
   */
  Map<String, String> devfileFileLocations();
}
