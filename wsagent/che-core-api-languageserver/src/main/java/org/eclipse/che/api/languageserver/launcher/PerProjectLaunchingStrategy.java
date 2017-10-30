<<<<<<< HEAD
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
package org.eclipse.che.api.languageserver.launcher;

import java.nio.file.Paths;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
=======
package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.vfs.Path;
>>>>>>> Introduce LaunchingStrategy to allow per project/workspace LS's

public class PerProjectLaunchingStrategy implements LaunchingStrategy {
  public static final PerProjectLaunchingStrategy INSTANCE = new PerProjectLaunchingStrategy();

  private PerProjectLaunchingStrategy() {}

  @Override
  public String getLaunchKey(String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
<<<<<<< HEAD
    return Paths.get(path).getName(0).toString();
=======
    return Path.of(path).element(0);
>>>>>>> Introduce LaunchingStrategy to allow per project/workspace LS's
  }

  @Override
  public boolean isApplicable(String launchKey, String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
<<<<<<< HEAD
    String project = Paths.get(path).getName(0).toString();
=======
    String project = Path.of(path).element(0);
>>>>>>> Introduce LaunchingStrategy to allow per project/workspace LS's
    return project.equals(launchKey);
  }

  @Override
  public String getRootUri(String fileUri) throws LanguageServerException {
    return LanguageServiceUtils.extractProjectPath(fileUri);
  }
}
