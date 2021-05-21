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
package org.eclipse.che.api.factory.server.urlfactory;

import static java.util.Collections.singletonList;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link RemoteFactoryUrl} which used with all factory URL's until there
 * is no specific implementation for given URL.
 */
public class DefaultFactoryUrl implements RemoteFactoryUrl {

  private String devfileFileLocation;

  @Override
  public String getProviderName() {
    return "default";
  }

  @Override
  public List<DevfileLocation> devfileFileLocations() {
    return singletonList(
        new DevfileLocation() {
          @Override
          public Optional<String> filename() {
            return Optional.empty();
          }

          @Override
          public String location() {
            return devfileFileLocation;
          }
        });
  }

  @Override
  public String rawFileLocation(String filename) {
    return URI.create(devfileFileLocation).resolve(filename).toString();
  }

  @Override
  public String getHostName() {
    return URI.create(devfileFileLocation).getHost();
  }

  @Override
  public String getBranch() {
    return null;
  }

  public DefaultFactoryUrl withDevfileFileLocation(String devfileFileLocation) {
    this.devfileFileLocation = devfileFileLocation;
    return this;
  }
}
