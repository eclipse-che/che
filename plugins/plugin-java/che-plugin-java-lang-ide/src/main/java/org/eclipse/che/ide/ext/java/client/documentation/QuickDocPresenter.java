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
package org.eclipse.che.ide.ext.java.client.documentation;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class QuickDocPresenter implements QuickDocumentation, QuickDocView.ActionDelegate {

    private final QuickDocView       view;
    private final AppContext         appContext;
    private final EditorAgent        editorAgent;
    private final WsAgentURLModifier agentURLDecorator;

    @Inject
    public QuickDocPresenter(QuickDocView view,
                             AppContext appContext,
                             EditorAgent editorAgent,
                             WsAgentURLModifier linksDecorator) {
        this.view = view;
        this.appContext = appContext;
        this.editorAgent = editorAgent;
        this.agentURLDecorator = linksDecorator;
    }

    @Override
    public void showDocumentation() {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null) {
            return;
        }

        if (!(activeEditor instanceof TextEditor)) {
            Log.error(getClass(), "Quick Document support only TextEditor as editor");
            return;
        }

        TextEditor editor = ((TextEditor)activeEditor);
        int offset = editor.getCursorOffset();
        final PositionConverter.PixelCoordinates coordinates = editor.getPositionConverter().offsetToPixel(offset);

        final Resource resource = appContext.getResource();

        if (resource != null) {
            final Optional<Project> project = resource.getRelatedProject();

            final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

            if (!srcFolder.isPresent()) {
                return;
            }

            final String fqn = JavaUtil.resolveFQN((Container)srcFolder.get(), resource);

            final String docUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/java/javadoc/find?fqn=" + fqn + "&projectpath=" + project.get().getLocation() + "&offset=" + offset;

            view.show(agentURLDecorator.modify(docUrl), coordinates.getX(), coordinates.getY());
        }


    }

    @Override
    public void onCloseView() {
    }
}
