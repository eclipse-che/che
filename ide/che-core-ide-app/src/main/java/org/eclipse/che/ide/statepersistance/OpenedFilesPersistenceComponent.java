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
package org.eclipse.che.ide.statepersistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.actions.OpenFileAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.api.statepersistance.dto.ActionDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.actions.OpenFileAction.FILE_PARAM_ID;

/**
 * Component provides sequence of actions which should be performed
 * in order to re-open all previously opened files and active (last opened) file.
 * <p>Note that all related project nodes will be expanded. This is project tree feature.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class OpenedFilesPersistenceComponent implements PersistenceComponent {
    private final Provider<EditorAgent> editorAgentProvider;
    private final ActionManager         actionManager;
    private final OpenFileAction        openFileAction;
    private final DtoFactory            dtoFactory;

    @Inject
    public OpenedFilesPersistenceComponent(Provider<EditorAgent> editorAgentProvider,
                                           ActionManager actionManager,
                                           OpenFileAction openFileAction,
                                           DtoFactory dtoFactory) {
        this.editorAgentProvider = editorAgentProvider;
        this.actionManager = actionManager;
        this.openFileAction = openFileAction;
        this.dtoFactory = dtoFactory;
    }

    @Override
    public List<ActionDescriptor> getActions() {
        final List<ActionDescriptor> actions = new ArrayList<>();
        final EditorAgent editorAgent = editorAgentProvider.get();
        final String openFileActionId = actionManager.getId(openFileAction);

        for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
            final VirtualFile file = editor.getEditorInput().getFile();

            if (file instanceof Resource) {
                actions.add(dtoFactory.createDto(ActionDescriptor.class)
                                      .withId(openFileActionId)
                                      .withParameters(Collections.singletonMap(FILE_PARAM_ID, file.getLocation().toString())));
            }
        }

        return actions;
    }
}
