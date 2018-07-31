/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides docker api version path prefix that should be used in Docker API calls. See
 * {@link DockerConnector}.
 *
 * @author Alexander Andrienko
 */
@Singleton
public class DockerApiVersionPathPrefixProvider implements Provider<String> {

  // Supports docker api version value in format: '1', '1.18' etc.
  private static final Pattern VERSION_API_PATTERN = Pattern.compile("([0-9]+)(\\.[0-9]+)?");
  private static final String VALID_PROPERTY_EXAMPLE =
      "Valid docker api version contains digits which can be separated by symbol '.'. For example: '1', '1.18'";

  public static final String MACHINE_DOCKER_API_VERSION = "che.docker.api";

  private final String apiVersionPrefixPath;

  @Inject
  public DockerApiVersionPathPrefixProvider(@Named(MACHINE_DOCKER_API_VERSION) String apiVersion) {
    if (apiVersion.isEmpty()) {
      this.apiVersionPrefixPath = "";
      return;
    }
    Matcher matcher = VERSION_API_PATTERN.matcher(apiVersion);
    if (matcher.matches()) {
      this.apiVersionPrefixPath = "/v" + apiVersion;
      return;
    }
    throw new IllegalArgumentException(
        format(
            "Invalid property format: '%s'. %s",
            MACHINE_DOCKER_API_VERSION, VALID_PROPERTY_EXAMPLE));
  }

  @Override
  public String get() {
    return apiVersionPrefixPath;
  }
}
