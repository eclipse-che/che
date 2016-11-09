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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

import javax.validation.constraints.NotNull;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * The Project Explorer view has a Link with Editor feature.
 * </p>
 * This can be enabled in header of the Project Explorer view by choosing Link with editor button.
 * If Link wih Editor is enabled - the current file open in the Editor will be highlighted in Project Explorer.
 */
@Singleton
public class LinkWithEditorAction extends AbstractPerspectiveAction {
    public static final String LINK_WITH_EDITOR = "linkWithEditor";

    private final Provider<EditorAgent> editorAgentProvider;
    private final EventBus              eventBus;
    private final PreferencesManager    preferencesManager;

    @Inject
    public LinkWithEditorAction(CoreLocalizationConstant localizationConstant,
                                Provider<EditorAgent> editorAgentProvider,
                                EventBus eventBus,
                                PreferencesManager preferencesManager) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.actionLinkWithEditor(),
              localizationConstant.actionLinkWithEditor(),
              null,
              null);
        this.editorAgentProvider = editorAgentProvider;
        this.eventBus = eventBus;
        this.preferencesManager = preferencesManager;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String linkWithEditorValue = preferencesManager.getValue(LINK_WITH_EDITOR);
        boolean value = !parseBoolean(linkWithEditorValue);
        preferencesManager.setValue(LINK_WITH_EDITOR, Boolean.toString(value));

        if (!value) {
            return;
        }

        final EditorPartPresenter activeEditor = editorAgentProvider.get().getActiveEditor();
        if (activeEditor == null) {
            return;
        }
        final EditorInput editorInput = activeEditor.getEditorInput();
        if (editorInput == null) {
            return;
        }
        eventBus.fireEvent(new RevealResourceEvent(editorInput.getFile().getLocation()));
    }
}
