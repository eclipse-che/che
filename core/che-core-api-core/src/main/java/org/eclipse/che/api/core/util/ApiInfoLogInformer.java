/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.util;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints information about Eclipse Che Core implementation, such as: version, scm revision, build
 * info.
 */
public class ApiInfoLogInformer {
  private static final Logger LOG = LoggerFactory.getLogger(ApiInfoLogInformer.class);

  private final ApiInfo apiInfo;

  @Inject
  public ApiInfoLogInformer(ApiInfo apiInfo) {
    this.apiInfo = apiInfo;
  }

  @PostConstruct
  public void printApiInfoOnStart() {
    LOG.info(
        "Eclipse Che Api Core: Build info '{}' scmRevision '{}' implementationVersion '{}'",
        apiInfo.getBuildInfo(),
        apiInfo.getScmRevision(),
        apiInfo.getImplementationVersion());
  }
}
