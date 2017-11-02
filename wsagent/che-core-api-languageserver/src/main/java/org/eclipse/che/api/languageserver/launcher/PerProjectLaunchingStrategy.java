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
package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.vfs.Path;

public class PerProjectLaunchingStrategy implements LaunchingStrategy {
  public static final PerProjectLaunchingStrategy INSTANCE = new PerProjectLaunchingStrategy();

  private PerProjectLaunchingStrategy() {}

  @Override
  public String getLaunchKey(String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
    return Path.of(path).element(0);
  }

  @Override
  public boolean isApplicable(String launchKey, String fileUri) {
    String path = LanguageServiceUtils.removePrefixUri(fileUri);
    String project = Path.of(path).element(0);
    return project.equals(launchKey);
  }

  @Override
  public String getRootUri(String fileUri) throws LanguageServerException {
    return LanguageServiceUtils.extractProjectPath(fileUri);
  }
}
