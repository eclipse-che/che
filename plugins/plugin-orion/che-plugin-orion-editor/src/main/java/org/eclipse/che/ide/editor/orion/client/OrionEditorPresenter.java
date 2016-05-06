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

import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelDataOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelGroupOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionLinkedModelOverlay;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelEvent;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelHandler;
import org.eclipse.che.ide.api.editor.annotation.ClearAnnotationModelEvent;
import org.eclipse.che.ide.api.editor.annotation.ClearAnnotationModelHandler;
import org.eclipse.che.ide.api.editor.annotation.HasAnnotationRendering;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistantFactory;
import org.eclipse.che.ide.api.editor.codeassist.HasCompletionInformation;
import org.eclipse.che.ide.api.debug.BreakpointRendererFactory;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.filetype.FileTypeIdentifier;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.HasGutter;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.api.editor.quickfix.QuickAssistantFactory;
import org.eclipse.che.ide.api.editor.texteditor.EditorModule;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPartView;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

/**
 * {@link TextEditorPresenter} using orion.
 * This class is only defined to allow the Gin binding to be performed.
 */
public class OrionEditorPresenter extends TextEditorPresenter<OrionEditorWidget> implements HasAnnotationRendering,
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
                                final EditorLocalizationConstants constant,
                                @Assisted final EditorWidgetFactory<OrionEditorWidget> editorWigetFactory,
                                final EditorModule<OrionEditorWidget> editorModule,
                                final TextEditorPartView editorView,
                                final EventBus eventBus,
                                final FileTypeIdentifier fileTypeIdentifier,
                                final QuickAssistantFactory quickAssistantFactory,
                                final WorkspaceAgent workspaceAgent,
                                final NotificationManager notificationManager) {
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
              workspaceAgent,
              notificationManager);
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
