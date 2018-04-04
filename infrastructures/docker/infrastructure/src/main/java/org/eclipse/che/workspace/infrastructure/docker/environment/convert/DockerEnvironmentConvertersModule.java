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
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.ComposeEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile.DockerfileEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;

/** @author Alexander Garagatyi */
public class DockerEnvironmentConvertersModule extends AbstractModule {
  @Override
  protected void configure() {
    // Environment type
    MapBinder<String, DockerEnvironmentConverter> envParserMapBinder =
        MapBinder.newMapBinder(binder(), String.class, DockerEnvironmentConverter.class);
    envParserMapBinder.addBinding(ComposeEnvironment.TYPE).to(ComposeEnvironmentConverter.class);
    envParserMapBinder
        .addBinding(DockerfileEnvironment.TYPE)
        .to(DockerfileEnvironmentConverter.class);
    envParserMapBinder
        .addBinding(DockerImageEnvironment.TYPE)
        .to(DockerImageEnvironmentConverter.class);
  }
}
