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
package org.eclipse.che.selenium.core.webdriver;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with files which are uploaded by WebDriver.
 *
 * @author Dmytro Nochevnov
 */
public interface UploadUtil {

  /**
   * Prepare local resource to be uploaded by WebDriver. If it's directory, it is zipped.
   *
   * @param seleniumWebDriver
   * @param localPath path to local file or directory which should be uploaded by WebDriver
   * @return path to file which WebDriver can upload
   */
  Path prepareToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) throws IOException;
}
