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
package org.eclipse.che.plugin.maven.client.comunnication;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.reconciler.Reconciler;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.maven.client.editor.PomReconcilingStrategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.api.editor.partition.DocumentPartitioner.DEFAULT_CONTENT_TYPE;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class PomEditorReconciler {

    private final EditorAgent editorAgent;

    @Inject
    public PomEditorReconciler(EditorAgent editorAgent) {
        this.editorAgent = editorAgent;
    }

    public void reconcilePoms(final List<String> updatedProjects) {
        new Timer(){

            @Override
            public void run() {
                Set<String> pomPaths = getPomPath(updatedProjects);
                List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
                for (EditorPartPresenter openedEditor : openedEditors) {
                    String path = openedEditor.getEditorInput().getFile().getLocation().toString();
                    if (pomPaths.contains(path)) {
                        if (openedEditor instanceof TextEditor) {
                            final Reconciler reconciler = ((TextEditor)openedEditor).getConfiguration().getReconciler();
                            if (reconciler != null) {
                                final ReconcilingStrategy strategy = reconciler.getReconcilingStrategy(DEFAULT_CONTENT_TYPE);
                                if (strategy instanceof PomReconcilingStrategy) {
                                    ((PomReconcilingStrategy)strategy).doReconcile();
                                }
                            }
                        }
                    }
                }
            }
        }.schedule(2000);
    }

    private Set<String> getPomPath(List<String> updatedProjects) {
        Set<String> result = new HashSet<>();
        for (String projectPath : updatedProjects) {
            String pomPath = projectPath.endsWith("/") ? projectPath + "pom.xml" : projectPath + "/pom.xml";
            result.add(pomPath);
        }

        return result;
    }


}
