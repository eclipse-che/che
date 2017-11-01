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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides MAVEN_OPTS environment variable with a value set to either maven options from
 * configuration or java options if maven options are not set.
 *
 * @author Yevhenii Voevodin
 */
public class MavenOptsEnvVariableProvider implements EnvVarProvider {

  @Inject
  @Named("che.workspace.java.options")
  private String javaOpts;

  @Inject
  @Named("che.workspace.maven.options")
  @Nullable
  private String mavenOpts;

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
    return Pair.of("MAVEN_OPTS", mavenOpts == null ? javaOpts : mavenOpts);
  }
}
