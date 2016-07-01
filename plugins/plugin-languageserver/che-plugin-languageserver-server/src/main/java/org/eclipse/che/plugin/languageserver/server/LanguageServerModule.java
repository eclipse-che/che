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
import org.eclipse.che.plugin.languageserver.server.csharp.CSharpLanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.json.JsonLanguageServerFactory;
import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;

@DynaModule
public class LanguageServerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), LanguageServerFactory.class).addBinding().to(JsonLanguageServerFactory.class);
        Multibinder.newSetBinder(binder(), LanguageServerFactory.class).addBinding().to(CSharpLanguageServerFactory.class);
//        Multibinder.newSetBinder(binder(), LanguageServerFactory.class).addBinding().to(FatJarBasedLanguageServerFactory.class);

        bind(LanguageServerRegistry.class).to(LanguageServerRegistryImpl.class);

        bind(LanguageRegistryService.class);
        bind(TextDocumentServiceImpl.class);
        bind(WorkspaceServiceImpl.class);
        bind(PublishDiagnosticsParamsMessenger.class);
    }

}
