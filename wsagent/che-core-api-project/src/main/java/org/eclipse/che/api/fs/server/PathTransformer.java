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
package org.eclipse.che.api.fs.server;

import java.nio.file.Path;

/** Transform workspace paths to file system paths */
public interface PathTransformer {

  /**
   * Transform workspace path to file system path
   *
   * @param wsPath absolute workspace path
   * @return
   */
  Path transform(String wsPath);

  /**
   * Transform file system path to workspace path
   *
   * @param fsPath file system apth
   * @return
   */
  String transform(Path fsPath);
}
