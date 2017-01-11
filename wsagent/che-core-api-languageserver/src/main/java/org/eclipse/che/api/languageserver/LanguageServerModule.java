/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver;

import org.eclipse.che.api.languageserver.messager.InitializeEventMessenger;
import org.eclipse.che.api.languageserver.messager.PublishDiagnosticsParamsMessenger;
import org.eclipse.che.api.languageserver.messager.ShowMessageMessenger;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistryImpl;
import org.eclipse.che.api.languageserver.registry.ServerInitializer;
import org.eclipse.che.api.languageserver.registry.ServerInitializerImpl;
import org.eclipse.che.api.languageserver.service.LanguageRegistryService;
import org.eclipse.che.api.languageserver.service.TextDocumentService;
import org.eclipse.che.api.languageserver.service.WorkspaceService;
import org.eclipse.che.inject.DynaModule;

import com.google.inject.AbstractModule;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LanguageServerRegistry.class).to(LanguageServerRegistryImpl.class);
        bind(ServerInitializer.class).to(ServerInitializerImpl.class);
        bind(LanguageRegistryService.class);
        bind(TextDocumentService.class);
        bind(WorkspaceService.class);
        bind(PublishDiagnosticsParamsMessenger.class);
        bind(ShowMessageMessenger.class);
        bind(InitializeEventMessenger.class);
    }
}
