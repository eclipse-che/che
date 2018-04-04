/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
