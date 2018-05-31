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
package org.eclipse.che.plugin.java.inject;

import static com.google.inject.multibindings.MapBinder.newMapBinder;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.languageserver.LanguageServerConfig;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerJsonRpcMessenger;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerLauncher;
import org.eclipse.che.plugin.java.languageserver.ProjectsListener;

/** @author Anatolii Bazko */
@DynaModule
public class JavaModule extends AbstractModule {

  public static final String LS_ID = "org.eclipse.che.plugin.java.languageserver";

  @Override
  protected void configure() {
    newMapBinder(binder(), String.class, LanguageServerConfig.class)
        .addBinding(LS_ID)
        .to(JavaLanguageServerLauncher.class)
        .asEagerSingleton();

    bind(JavaLanguageServerJsonRpcMessenger.class).asEagerSingleton();
    bind(JavaLanguageServerExtensionService.class).asEagerSingleton();
    bind(ProjectsListener.class).asEagerSingleton();
  }
}
