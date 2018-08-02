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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.Collections;
import org.eclipse.che.plugin.java.server.projecttype.AbstractJavaInitHandler;
import org.eclipse.che.plugin.maven.server.core.MavenWorkspace;
import org.eclipse.jdt.core.IJavaProject;

/** @author Vitaly Parfonov */
@Singleton
public class MavenProjectInitHandler extends AbstractJavaInitHandler {

  private final Provider<MavenWorkspace> mavenWorkspace;

  @Inject
  public MavenProjectInitHandler(Provider<MavenWorkspace> mavenWorkspace) {
    this.mavenWorkspace = mavenWorkspace;
  }

  @Override
  public String getProjectType() {
    return MAVEN_ID;
  }

  @Override
  protected void initializeClasspath(IJavaProject javaProject) {
    mavenWorkspace.get().update(Collections.singletonList(javaProject.getProject()));
  }
}
