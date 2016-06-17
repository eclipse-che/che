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

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistant;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantImpl;
import org.eclipse.che.ide.debug.BreakpointManagerImpl;
import org.eclipse.che.ide.api.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.debug.BreakpointRendererImpl;
import org.eclipse.che.ide.api.editor.defaulteditor.DefaultTextEditorProvider;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.api.editor.filetype.MultipleMethodFileIdentifier;
import org.eclipse.che.ide.editor.texteditor.infopanel.InfoPanel;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMapImpl;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistAssistant;
import org.eclipse.che.ide.editor.quickfix.QuickAssistAssistantImpl;
import org.eclipse.che.ide.editor.quickfix.QuickAssistWidgetFactory;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerFactory;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.editor.texteditor.TextEditorPartViewImpl;

@ExtensionGinModule
public class EditorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
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

        // breakpoint renderer and manager
        install(new GinFactoryModuleBuilder()
                    .implement(BreakpointRenderer.class, BreakpointRendererImpl.class)
                    .build(BreakpointRendererFactory.class));
        bind(BreakpointManager.class).to(BreakpointManagerImpl.class).in(Singleton.class);

        // bind the quick assist widget factory
        install(new GinFactoryModuleBuilder()
                        .build(QuickAssistWidgetFactory.class));
    }
}
