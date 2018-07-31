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
package org.eclipse.che.plugin.java.server.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.nio.file.Paths;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.server.rest.JavaFormatterService;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.ResourcesPlugin;

/** @author Evgen Vidolob */
@DynaModule
public class JdtGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(JavaFormatterService.class);
    bind(ResourcesPlugin.class).asEagerSingleton();
    bind(FileBuffersPlugin.class).asEagerSingleton();
  }

  @Provides
  @Named("che.jdt.settings.dir")
  @Singleton
  protected String provideSettings(@Named("che.workspace.metadata") String wsMetadata) {
    return Paths.get(System.getProperty("user.home"), wsMetadata, "settings").toString();
  }

  @Provides
  @Named("che.jdt.workspace.index.dir")
  @Singleton
  protected String provideIndex(@Named("che.workspace.metadata") String wsMetadata) {
    return Paths.get(System.getProperty("user.home"), wsMetadata, "index").toString();
  }
}
