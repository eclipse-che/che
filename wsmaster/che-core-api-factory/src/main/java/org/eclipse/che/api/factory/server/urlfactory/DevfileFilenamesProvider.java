/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import com.google.common.base.Splitter;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** Provides list of configured devfile filenames to look in repository-based factories. */
@Singleton
public class DevfileFilenamesProvider {

  private final List<String> configuredDevfileFilenames;

  @Inject
  public DevfileFilenamesProvider(
      @Named("che.factory.default_devfile_filenames") String devfileFilenames) {
    this.configuredDevfileFilenames = Splitter.on(",").splitToList(devfileFilenames);
  }

  public List<String> getConfiguredDevfileFilenames() {
    return configuredDevfileFilenames;
  }
}
