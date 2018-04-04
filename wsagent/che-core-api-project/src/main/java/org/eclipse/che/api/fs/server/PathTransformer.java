/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
