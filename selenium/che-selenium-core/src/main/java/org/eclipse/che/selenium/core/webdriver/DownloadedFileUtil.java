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

import java.io.IOException;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with files which are downloaded by WebDriver.
 *
 * @author Dmytro Nochevnov
 */
public interface DownloadedFileUtil {

  /**
   * Get list of files which package holds.
   *
   * @param seleniumWebDriver which operates with UI
   * @param downloadedPackageName downloaded package to unzip
   * @return list of files which package holds
   */
  List<String> getPackageFileList(SeleniumWebDriver seleniumWebDriver, String downloadedPackageName)
      throws IOException;

  /**
   * Obtains content of downloaded file.
   *
   * @param seleniumWebDriver which operates with UI
   * @param downloadedFileName downloaded file name
   * @return content of file
   */
  String getDownloadedFileContent(SeleniumWebDriver seleniumWebDriver, String downloadedFileName)
      throws IOException;

  /**
   * Removes downloaded file.
   *
   * @param seleniumWebDriver which operates with UI
   * @param filenames downloaded file name
   */
  void removeDownloadedFiles(SeleniumWebDriver seleniumWebDriver, String... filenames)
      throws IOException;
}
