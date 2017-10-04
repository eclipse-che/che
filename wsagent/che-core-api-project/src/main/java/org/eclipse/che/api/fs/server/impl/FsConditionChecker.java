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
package org.eclipse.che.api.fs.server.impl;

import java.nio.file.Path;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsConditionChecker {


  public static void mustBeADirectory(Path fsPath) throws ConflictException {
    if (!fsPath.toFile().isDirectory()) {
      throw new ConflictException("FS item '" + fsPath.toString() + "' must be a directory");
    }
  }

}
