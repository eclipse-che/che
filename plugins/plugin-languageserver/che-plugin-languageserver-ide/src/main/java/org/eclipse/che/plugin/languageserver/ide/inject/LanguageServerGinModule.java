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
package org.eclipse.che.plugin.languageserver.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.eclipse.che.ide.filetypes.FileTypeProviderImpl;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerAnnotationModelFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerCodeassistProcessorFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfigurationFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerFormatterFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerReconcileStrategyFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.LanguageServerQuickAssistProcessorFactory;
import org.eclipse.che.plugin.languageserver.ide.editor.signature.LanguageServerSignatureHelpFactory;
import org.eclipse.che.plugin.languageserver.ide.location.OpenLocationPresenterFactory;
import org.eclipse.che.plugin.languageserver.ide.rename.node.RenameNodeFactory;
import org.eclipse.che.plugin.languageserver.ide.service.ExecuteClientCommandReceiver;
import org.eclipse.che.plugin.languageserver.ide.service.PublishDiagnosticsReceiver;
import org.eclipse.che.plugin.languageserver.ide.service.ShowMessageJsonRpcReceiver;

/** @author Anatolii Bazko */
@ExtensionGinModule
public class LanguageServerGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    install(new GinFactoryModuleBuilder().build(LanguageServerAnnotationModelFactory.class));
    install(new GinFactoryModuleBuilder().build(OpenLocationPresenterFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerEditorConfigurationFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerFormatterFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerCodeassistProcessorFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerQuickAssistProcessorFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerReconcileStrategyFactory.class));
    install(new GinFactoryModuleBuilder().build(LanguageServerSignatureHelpFactory.class));
    install(new GinFactoryModuleBuilder().build(RenameNodeFactory.class));

    bind(PublishDiagnosticsReceiver.class).asEagerSingleton();
    bind(ShowMessageJsonRpcReceiver.class).asEagerSingleton();
    bind(ExecuteClientCommandReceiver.class).asEagerSingleton();

    GinMultibinder.newSetBinder(binder(), LanguageDescription.class);

    bind(FileTypeProvider.class).to(FileTypeProviderImpl.class).in(Singleton.class);
  }
}
