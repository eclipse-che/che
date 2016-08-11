/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.languageserver.server.launcher.CSharpLanguageServerLauncher;
import org.eclipse.che.plugin.languageserver.server.launcher.JsonLanguageServerLauncher;
import org.eclipse.che.plugin.languageserver.server.launcher.LanguageServerLauncher;
import org.eclipse.che.plugin.languageserver.server.messager.InitializeEventMessenger;
import org.eclipse.che.plugin.languageserver.server.messager.PublishDiagnosticsParamsMessenger;
import org.eclipse.che.plugin.languageserver.server.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.server.registry.LanguageServerRegistryImpl;
import org.eclipse.che.plugin.languageserver.server.registry.ServerInitializer;
import org.eclipse.che.plugin.languageserver.server.registry.ServerInitializerImpl;
import org.eclipse.che.plugin.languageserver.server.service.LanguageRegistryService;
import org.eclipse.che.plugin.languageserver.server.service.TextDocumentService;
import org.eclipse.che.plugin.languageserver.server.service.WorkspaceService;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(JsonLanguageServerLauncher.class);
        Multibinder.newSetBinder(binder(), LanguageServerLauncher.class).addBinding().to(CSharpLanguageServerLauncher.class);

        bind(LanguageServerRegistry.class).to(LanguageServerRegistryImpl.class);
        bind(ServerInitializer.class).to(ServerInitializerImpl.class);

        bind(LanguageRegistryService.class);
        bind(TextDocumentService.class);
        bind(WorkspaceService.class);
        bind(PublishDiagnosticsParamsMessenger.class);
        bind(InitializeEventMessenger.class);
    }
}
