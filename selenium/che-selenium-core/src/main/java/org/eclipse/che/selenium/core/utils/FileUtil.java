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
package org.eclipse.che.selenium.core.utils;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Singleton;

/** @author Dmytro Nochevnov */
@Singleton
public class FileUtil {

  public void removeEmptyDirectory(Path directory) throws IOException {
    if (Files.exists(directory)
        && Files.isDirectory(directory)
        && Files.list(directory).collect(toList()).isEmpty()) {

      Files.delete(directory);
    }
  }
}
