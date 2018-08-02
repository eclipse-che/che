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
package org.eclipse.che.api.git;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.git.shared.GitUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves git user from environment preferences.
 *
 * @author Max Shaposhnik
 */
@Singleton
public class LocalGitUserResolver implements GitUserResolver {

  private static final Logger LOG = LoggerFactory.getLogger(LocalGitUserResolver.class);

  private final String apiUrl;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public LocalGitUserResolver(
      @Named("che.api") String apiUrl, HttpJsonRequestFactory requestFactory) {
    this.apiUrl = apiUrl;
    this.requestFactory = requestFactory;
  }

  @Override
  public GitUser getUser() {
    String name = null;
    String email = null;
    try {
      Map<String, String> preferences =
          requestFactory
              .fromUrl(apiUrl + "/preferences")
              .useGetMethod()
              .addQueryParam("filter", "git.committer.\\w+")
              .request()
              .asProperties();
      name = preferences.get("git.committer.name");
      email = preferences.get("git.committer.email");
    } catch (ApiException | IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
    GitUser gitUser = newDto(GitUser.class);
    if (!isNullOrEmpty(name)) {
      gitUser.setName(name);
    }
    if (!isNullOrEmpty(email)) {
      gitUser.setEmail(email);
    }
    return gitUser;
  }
}
