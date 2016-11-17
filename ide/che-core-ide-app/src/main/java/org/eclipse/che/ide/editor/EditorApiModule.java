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
package org.eclipse.che.ide.editor;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistant;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantImpl;
import org.eclipse.che.ide.api.editor.defaulteditor.DefaultTextEditorProvider;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.api.editor.filetype.MultipleMethodFileIdentifier;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMapImpl;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistAssistant;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerFactory;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.editor.quickfix.QuickAssistAssistantImpl;
import org.eclipse.che.ide.editor.quickfix.QuickAssistWidgetFactory;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizer;
import org.eclipse.che.ide.editor.synchronization.EditorContentSynchronizerImpl;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSychronizationFactory;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSynchronization;
import org.eclipse.che.ide.editor.synchronization.EditorGroupSynchronizationImpl;
import org.eclipse.che.ide.editor.texteditor.TextEditorPartViewImpl;
import org.eclipse.che.ide.editor.texteditor.infopanel.InfoPanel;
import org.eclipse.che.ide.part.editor.EditorPartStackView;
import org.eclipse.che.ide.part.editor.EditorTabContextMenuFactory;
import org.eclipse.che.ide.part.editor.recent.RecentFileActionFactory;
import org.eclipse.che.ide.part.editor.recent.RecentFileList;
import org.eclipse.che.ide.part.editor.recent.RecentFileStore;
import org.eclipse.che.ide.util.executor.UserActivityManager;

/**
 * GIN module for configuring Editor API components.
 *
 * @author Artem Zatsarynnyi
 */
public class EditorApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(EditorAgent.class).to(EditorAgentImpl.class).in(Singleton.class);

        bind(UserActivityManager.class).in(Singleton.class);

        bind(EditorRegistry.class).to(EditorRegistryImpl.class).in(Singleton.class);

        bind(EditorPartStackView.class);

        bind(EditorContentSynchronizer.class).to(EditorContentSynchronizerImpl.class).in(Singleton.class);

        install(new GinFactoryModuleBuilder()
                        .implement(EditorGroupSynchronization.class, EditorGroupSynchronizationImpl.class)
                        .build(EditorGroupSychronizationFactory.class));

        // the text editor view
        bind(TextEditorPartView.class).to(TextEditorPartViewImpl.class);

        // Bind the file type identifier
        bind(FileTypeIdentifier.class).to(MultipleMethodFileIdentifier.class);

        // bind the document storage
        bind(DocumentStorage.class);

        // bind the default editor
        bind(EditorProvider.class).annotatedWith(Names.named("defaultEditor")).to(DefaultTextEditorProvider.class);

        // bind the info panel
        bind(InfoPanel.class);

        // bind the document position model
        bind(DocumentPositionMap.class).to(DocumentPositionMapImpl.class);

        // bind the reconciler
        install(new GinFactoryModuleBuilder()
                        .implement(Reconciler.class, ReconcilerWithAutoSave.class)
                        .build(ReconcilerFactory.class));

        // bind the code assistant and quick assistant
        install(new GinFactoryModuleBuilder()
                        .implement(CodeAssistant.class, CodeAssistantImpl.class)
                        .build(CodeAssistantFactory.class));
        install(new GinFactoryModuleBuilder()
                        .implement(QuickAssistAssistant.class, QuickAssistAssistantImpl.class)
                        .build(QuickAssistantFactory.class));

        // bind the quick assist widget factory
        install(new GinFactoryModuleBuilder().build(QuickAssistWidgetFactory.class));

        install(new GinFactoryModuleBuilder().build(EditorTabContextMenuFactory.class));

        install(new GinFactoryModuleBuilder().build(RecentFileActionFactory.class));
        bind(RecentFileList.class).to(RecentFileStore.class).in(Singleton.class);
    }
}
