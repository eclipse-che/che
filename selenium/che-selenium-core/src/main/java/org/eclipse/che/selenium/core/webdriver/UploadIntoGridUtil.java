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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.DockerUtil;

/**
 * This is set of methods to work with files which are uploaded by WebDriver inside selenium node.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class UploadIntoGridUtil extends AbstractUploadUtil {
  private final Path uploadDir;
  private final DockerUtil dockerUtil;

  @Inject
  public UploadIntoGridUtil(@Named("tests.tmp_dir") String uploadDir, DockerUtil dockerUtil) {
    this.uploadDir = Paths.get(uploadDir);
    this.dockerUtil = dockerUtil;
  }

  @Override
  Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) throws IOException {
    dockerUtil.copyIntoContainer(seleniumWebDriver.getGridNodeContainerId(), localPath, uploadDir);

    return uploadDir.resolve(localPath.getFileName());
  }
}
