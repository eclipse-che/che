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

import static java.util.Collections.singletonMap;

import java.util.Map;

/**
 * Default implementation of {@link RemoteFactoryUrl} which used with all factory URL's until there
 * is no specific implementation for given URL.
 */
public class DefaultFactoryUrl implements RemoteFactoryUrl {

  private String devfileFileLocation;
  private String factoryFilename;
  private String factoryFileLocation;

  @Override
  public String getFactoryFilename() {
    return factoryFilename;
  }

  public DefaultFactoryUrl withFactoryFilename(String factoryFilename) {
    this.factoryFilename = factoryFilename;
    return this;
  }

  @Override
  public String factoryFileLocation() {
    return factoryFileLocation;
  }

  public DefaultFactoryUrl withFactoryFileLocation(String factoryFileLocation) {
    this.factoryFileLocation = factoryFileLocation;
    return this;
  }

  @Override
  public Map<String, String> devfileFileLocations() {
    // since for general URL's we didn't have exact filename and always have only one
    // location, so this kind of map is ok here
    return singletonMap(null, devfileFileLocation);
  }

  public DefaultFactoryUrl withDevfileFileLocation(String devfileFileLocation) {
    this.devfileFileLocation = devfileFileLocation;
    return this;
  }
}
