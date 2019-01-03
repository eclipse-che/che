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
package org.eclipse.che.selenium.core;

import com.google.inject.AbstractModule;
import org.eclipse.che.selenium.core.webdriver.DownloadedFileUtil;
import org.eclipse.che.selenium.core.webdriver.DownloadedIntoGridFileUtil;
import org.eclipse.che.selenium.core.webdriver.DownloadedLocallyFileUtil;
import org.eclipse.che.selenium.core.webdriver.UploadIntoGridUtil;
import org.eclipse.che.selenium.core.webdriver.UploadLocallyUtil;
import org.eclipse.che.selenium.core.webdriver.UploadUtil;

public class TestExecutionModule extends AbstractModule {

  @Override
  protected void configure() {
    boolean gridMode = Boolean.valueOf(System.getProperty("grid.mode"));
    if (gridMode) {
      bind(DownloadedFileUtil.class).to(DownloadedIntoGridFileUtil.class);
      bind(UploadUtil.class).to(UploadIntoGridUtil.class);
    } else {
      bind(DownloadedFileUtil.class).to(DownloadedLocallyFileUtil.class);
      bind(UploadUtil.class).to(UploadLocallyUtil.class);
    }
  }
}
