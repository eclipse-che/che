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
package org.eclipse.che.plugin.maven.server.projecttype;

import javax.inject.Inject;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;

/** @author Evgen Vidolob */
public class MavenValueProviderFactory implements ValueProviderFactory {

  @Inject FsManager fsManager;

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new MavenValueProvider(wsPath, fsManager);
  }
}
