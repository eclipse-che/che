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
package org.eclipse.che.ide.jseditor.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.debug.BreakpointRenderer;
import org.eclipse.che.ide.jseditor.client.JsEditorConstants;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistant;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistantImpl;
import org.eclipse.che.ide.jseditor.client.debug.BreakpointManagerImpl;
import org.eclipse.che.ide.jseditor.client.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.jseditor.client.debug.BreakpointRendererImpl;
import org.eclipse.che.ide.jseditor.client.defaulteditor.DefaultTextEditorProvider;
import org.eclipse.che.ide.jseditor.client.document.DocumentStorage;
import org.eclipse.che.ide.jseditor.client.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.jseditor.client.filetype.MultipleMethodFileIdentifier;
import org.eclipse.che.ide.jseditor.client.infopanel.InfoPanel;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMap;
import org.eclipse.che.ide.jseditor.client.partition.DocumentPositionMapImpl;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistAssistant;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistAssistantImpl;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistWidgetFactory;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.jseditor.client.reconciler.Reconciler;
import org.eclipse.che.ide.jseditor.client.reconciler.ReconcilerFactory;
import org.eclipse.che.ide.jseditor.client.reconciler.ReconcilerWithAutoSave;
import org.eclipse.che.ide.jseditor.client.requirejs.ModuleHolder;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditorPartView;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditorPartViewImpl;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.inject.Singleton;

@ExtensionGinModule
public class JsEditorGinModule extends AbstractGinModule {

    /** The default text file type: text/plain. */
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    @Override
    protected void configure() {
        bind(ModuleHolder.class).in(Singleton.class);

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

    @Provides
    @Singleton
    @PlainTextFileType
    protected FileType textPlainFileType(final JsEditorConstants constants) {
        return new FileType(constants.defaultEditorDescription(),
                            (SVGResource)null,
                            CONTENT_TYPE_TEXT_PLAIN,
                            (String)null);
    }
}
