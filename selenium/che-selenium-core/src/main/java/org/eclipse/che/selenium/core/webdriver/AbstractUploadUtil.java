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
package org.eclipse.che.selenium.core.webdriver;

import static com.google.common.io.Files.createTempDir;
import static org.eclipse.che.commons.lang.ZipUtils.zipDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/** @author Dmytro Nochevnov */
public abstract class AbstractUploadUtil implements UploadUtil {
  private static final String ZIP_FILE_NAME = "upload.zip";

  @Override
  public final Path prepareToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException {
    if (!localPath.toFile().isFile()) {
      Path zipFile = Paths.get(createTempDir().toString()).resolve(ZIP_FILE_NAME);

      zipDir(localPath.toString(), localPath.toFile(), zipFile.toFile(), null);

      localPath = zipFile;
    }

    return prepareFileToUpload(seleniumWebDriver, localPath);
  }

  abstract Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPathtoFile)
      throws IOException;
}
