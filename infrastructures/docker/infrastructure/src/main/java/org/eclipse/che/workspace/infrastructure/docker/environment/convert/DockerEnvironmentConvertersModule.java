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
