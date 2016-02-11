/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.debug.BreakpointManager;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelDataOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelGroupOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelOverlay;
import org.eclipse.che.ide.jseditor.client.JsEditorConstants;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModelEvent;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModelHandler;
import org.eclipse.che.ide.jseditor.client.annotation.ClearAnnotationModelEvent;
import org.eclipse.che.ide.jseditor.client.annotation.ClearAnnotationModelHandler;
import org.eclipse.che.ide.jseditor.client.annotation.HasAnnotationRendering;
import org.eclipse.che.ide.jseditor.client.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.jseditor.client.codeassist.HasCompletionInformation;
import org.eclipse.che.ide.jseditor.client.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.jseditor.client.document.DocumentHandle;
import org.eclipse.che.ide.jseditor.client.document.DocumentStorage;
import org.eclipse.che.ide.jseditor.client.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.jseditor.client.gutter.Gutter;
import org.eclipse.che.ide.jseditor.client.gutter.HasGutter;
import org.eclipse.che.ide.jseditor.client.link.HasLinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedModel;
import org.eclipse.che.ide.jseditor.client.link.LinkedModelData;
import org.eclipse.che.ide.jseditor.client.link.LinkedModelGroup;
import org.eclipse.che.ide.jseditor.client.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.jseditor.client.texteditor.EditorModule;
import org.eclipse.che.ide.jseditor.client.texteditor.EditorWidget;
import org.eclipse.che.ide.jseditor.client.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPartView;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * {@link EmbeddedTextEditorPresenter} using orion.
 * This class is only defined to allow the Gin binding to be performed.
 */
public class OrionEditorPresenter extends EmbeddedTextEditorPresenter<OrionEditorWidget> implements HasAnnotationRendering,
                                                                                                    HasLinkedMode,
                                                                                                    HasCompletionInformation,
                                                                                                    HasGutter {

    private final AnnotationRendering rendering = new AnnotationRendering();

    @AssistedInject
    public OrionEditorPresenter(final CodeAssistantFactory codeAssistantFactory,
                                final BreakpointManager breakpointManager,
                                final BreakpointRendererFactory breakpointRendererFactory,
                                final DialogFactory dialogFactory,
                                final DocumentStorage documentStorage,
                                final JsEditorConstants constant,
                                @Assisted final EditorWidgetFactory<OrionEditorWidget> editorWigetFactory,
                                final EditorModule<OrionEditorWidget> editorModule,
                                final EmbeddedTextEditorPartView editorView,
                                final EventBus eventBus,
                                final FileTypeIdentifier fileTypeIdentifier,
                                final QuickAssistantFactory quickAssistantFactory,
                                final WorkspaceAgent workspaceAgent) {
        super(codeAssistantFactory,
              breakpointManager,
              breakpointRendererFactory,
              dialogFactory,
              documentStorage,
              constant,
              editorWigetFactory,
              editorModule,
              editorView,
              eventBus,
              fileTypeIdentifier,
              quickAssistantFactory,
              workspaceAgent);
    }

    @Override
    public void configure(AnnotationModel model, DocumentHandle document) {
        document.getDocEventBus().addHandler(AnnotationModelEvent.TYPE, rendering);
        document.getDocEventBus().addHandler(ClearAnnotationModelEvent.TYPE, rendering);
    }

    @Override
    public LinkedMode getLinkedMode() {
        EditorWidget editorWidget = getEditorWidget();
        if(editorWidget != null){
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            return orion.getLinkedMode();
        }
        return null;
    }

    @Override
    public LinkedModel createLinkedModel() {
        return OrionLinkedModelOverlay.create();
    }

    @Override
    public LinkedModelGroup createLinkedGroup() {
        return OrionLinkedModelGroupOverlay.create();
    }

    @Override
    public LinkedModelData createLinkedModelData() {
        return OrionLinkedModelDataOverlay.create();
    }

    @Override
    public void showCompletionInformation() {
        EditorWidget editorWidget = getEditorWidget();
        if(editorWidget != null){
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            orion.showCompletionInformation();
        }
    }

    @Override
    public Gutter getGutter() {
        final EditorWidget editorWidget = getEditorWidget();
        if (editorWidget instanceof HasGutter) {
            return ((HasGutter)editorWidget).getGutter();
        } else {
            throw new IllegalStateException("incorrect editor state");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus() {
        EditorWidget editorWidget = getEditorWidget();
        if(editorWidget != null){
            OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
            orion.setFocus();
        }

    }

    private class AnnotationRendering implements AnnotationModelHandler, ClearAnnotationModelHandler {

        @Override
        public void onAnnotationModel(AnnotationModelEvent event) {
            EditorWidget editorWidget = getEditorWidget();
            if(editorWidget != null){
                OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
                orion.showErrors(event);
            }
        }

        @Override
        public void onClearModel(ClearAnnotationModelEvent event) {
            EditorWidget editorWidget = getEditorWidget();
            if(editorWidget != null){
                OrionEditorWidget orion = ((OrionEditorWidget)editorWidget);
                orion.clearErrors();
            }
        }
    }
}
