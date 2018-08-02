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
package org.eclipse.che.plugin.github.factory.resolver;

import javax.inject.Inject;
import org.eclipse.che.plugin.urlfactory.URLChecker;

/**
 * Support old dockerfila and factory filenames;
 *
 * @author Max Shaposhnik
 */
public class LegacyGithubURLParser extends GithubURLParserImpl {

  private URLChecker urlChecker;

  @Inject
  public LegacyGithubURLParser(URLChecker urlChecker) {
    this.urlChecker = urlChecker;
  }

  @Override
  public GithubUrl parse(String url) {
    GithubUrl githubUrl = super.parse(url);
    if (!urlChecker.exists(githubUrl.dockerFileLocation())) {
      githubUrl.withDockerfileFilename(".codenvy.dockerfile");
    }

    if (!urlChecker.exists(githubUrl.factoryJsonFileLocation())) {
      githubUrl.withFactoryFilename(".codenvy.json");
    }
    return githubUrl;
  }
}
