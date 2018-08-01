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

import com.google.inject.Singleton;
import java.nio.file.Path;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with file which is uploaded by WebDriver locally.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class UploadLocallyUtil extends AbstractUploadUtil {

  @Override
  public Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) {
    return localPath;
  }
}
