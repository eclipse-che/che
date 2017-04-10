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
package org.eclipse.che.plugin.languageserver.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMapBinder;

import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerFileTypeRegister;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerAnnotationModelFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerCodeassistProcessorFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfigurationFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerFormatterFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerReconcileStrategyFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.signature.LanguageServerSignatureHelpFactory;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenterFactory;
import org.eclipse.che.plugin.languageserver.ide.registry.LanguageServerRegistry;

/**
 * @author Anatolii Bazko
 */
@ExtensionGinModule
public class LanguageServerGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        install(new GinFactoryModuleBuilder().build(LanguageServerAnnotationModelFactory.class));
        install(new GinFactoryModuleBuilder().build(OpenLocationPresenterFactory.class));
        install(new GinFactoryModuleBuilder().build(LanguageServerEditorConfigurationFactory.class));
        install(new GinFactoryModuleBuilder().build(LanguageServerFormatterFactory.class));
        install(new GinFactoryModuleBuilder().build(LanguageServerCodeassistProcessorFactory.class));
        install(new GinFactoryModuleBuilder().build(LanguageServerReconcileStrategyFactory.class));
        install(new GinFactoryModuleBuilder().build(LanguageServerSignatureHelpFactory.class));
        bind(LanguageServerRegistry.class);

        GinMapBinder<String, WsAgentComponent> wsAgentComponentsBinder =
                GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class);
        wsAgentComponentsBinder.addBinding("Load Language Server file types.").to(LanguageServerFileTypeRegister.class);
    }

}
