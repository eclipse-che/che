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
package org.eclipse.che.api.devfile.server;

import java.io.IOException;

/**
 * Some types of {@link org.eclipse.che.api.devfile.model.Tool} may have configuration located in a
 * separate file. It fetches content of file by its name.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public interface FileContentProvider {

  /**
   * Fetches content of the specified file.
   *
   * @param fileName file name to fetch content. Only devfile-relative files are currently
   *     supported, so it means file should be localed at the same directory level as devfile (no
   *     matter in repository or PR or branch etc )
   * @return content of the specified file
   * @throws IOException when there is an error during content retrieval
   */
  String fetchContent(String fileName) throws IOException;
}
