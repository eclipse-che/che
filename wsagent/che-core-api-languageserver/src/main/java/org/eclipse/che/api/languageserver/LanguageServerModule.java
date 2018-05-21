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
package org.eclipse.che.api.languageserver;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.eclipse.che.api.languageserver.messager.PublishDiagnosticsParamsJsonRpcTransmitter;
import org.eclipse.che.api.languageserver.messager.ShowMessageJsonRpcTransmitter;

public class LanguageServerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceService.class).asEagerSingleton();
    bind(TextDocumentService.class).asEagerSingleton();
    bind(PublishDiagnosticsParamsJsonRpcTransmitter.class).asEagerSingleton();
    bind(ShowMessageJsonRpcTransmitter.class).asEagerSingleton();
    bind(LanguageServerFileWatcher.class).asEagerSingleton();
    bind(LanguageServerConfigInitializer.class).asEagerSingleton();
    bind(LanguageServerService.class).asEagerSingleton();

    install(new FactoryModuleBuilder().build(CheLanguageClientFactory.class));

    newMapBinder(binder(), String.class, LanguageServerConfig.class);

    newSetBinder(binder(), LanguageServerConfigProvider.class)
        .addBinding()
        .to(WorkspaceConfigProvider.class);
    newSetBinder(binder(), LanguageServerConfigProvider.class)
        .addBinding()
        .to(GuiceConfigProvider.class);
  }
}
