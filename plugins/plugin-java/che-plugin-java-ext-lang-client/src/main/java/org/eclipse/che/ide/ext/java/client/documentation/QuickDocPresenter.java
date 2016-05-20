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
package org.eclipse.che.ide.ext.java.client.documentation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class QuickDocPresenter implements QuickDocumentation, QuickDocView.ActionDelegate {


    private final QuickDocView       view;
    private final AppContext         appContext;
    private final EditorAgent        editorAgent;

    @Inject
    public QuickDocPresenter(QuickDocView view,
                             AppContext appContext,
                             EditorAgent editorAgent) {
        this.view = view;
        this.appContext = appContext;
        this.editorAgent = editorAgent;
    }

    @Override
    public void showDocumentation() {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null) {
            return;
        }

        if (!(activeEditor instanceof TextEditorPresenter)) {
            Log.error(getClass(), "Quick Document support only TextEditorPresenter as editor");
            return;
        }

        TextEditorPresenter editor = ((TextEditorPresenter)activeEditor);
        int offset = editor.getCursorOffset();
        final PositionConverter.PixelCoordinates coordinates = editor.getPositionConverter().offsetToPixel(offset);
        view.show(appContext.getDevMachine().getWsAgentBaseUrl() + "/java/javadoc/find?fqn=" +
                  JavaSourceFolderUtil.getFQNForFile(editor.getEditorInput().getFile()) + "&projectpath=" +
                  appContext.getCurrentProject().getProjectConfig().getPath() + "&offset=" + offset, coordinates.getX(),
                  coordinates.getY());
    }

    @Override
    public void onCloseView() {
    }
}
