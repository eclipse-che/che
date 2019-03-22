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

/**
 * Default implementation of {@link RemoteFactoryUrl} which used with all factory URL's until there
 * is no specific implementation for given URL.
 */
public class DefaultFactoryUrl implements RemoteFactoryUrl {

  private String devfileFilename;
  private String devfileFileLocation;
  private String factoryFilename;
  private String factoryFileLocation;

  @Override
  public String getDevfileFilename() {
    return devfileFilename;
  }

  public DefaultFactoryUrl withDevfileFilename(String devfileFilename) {
    this.devfileFilename = devfileFilename;
    return this;
  }

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
  public String devfileFileLocation() {
    return devfileFileLocation;
  }

  public DefaultFactoryUrl withDevfileFileLocation(String devfileFileLocation) {
    this.devfileFileLocation = devfileFileLocation;
    return this;
  }
}
