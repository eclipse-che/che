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
package org.eclipse.che.plugin.maven.server.projecttype;

import javax.inject.Inject;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;

/** @author Evgen Vidolob */
public class MavenValueProviderFactory implements ValueProviderFactory {

  @Inject MavenProjectManager mavenProjectManager;
  @Inject FsManager fsManager;

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new MavenValueProvider(mavenProjectManager, wsPath, fsManager);
  }
}
