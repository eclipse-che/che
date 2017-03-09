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
package org.eclipse.che.ide.command.editor.page.context;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

/**
 * Presenter for {@link CommandEditorPage} which allows to edit command's applicable context.
 *
 * @author Artem Zatsarynnyi
 */
public class ContextPage extends AbstractCommandEditorPage implements ContextPageView.ActionDelegate {

    private final ContextPageView view;

    /** Initial value of the workspace flag. */
    private boolean workspaceInitial;

    @Inject
    public ContextPage(ContextPageView view, EditorMessages messages) {
        super(messages.pageContextTitle());

        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        final ApplicableContext context = editedCommand.getApplicableContext();

        workspaceInitial = context.isWorkspaceApplicable();

        view.setWorkspace(editedCommand.getApplicableContext().isWorkspaceApplicable());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        return !(workspaceInitial == applicableContext.isWorkspaceApplicable());
    }

    @Override
    public void onWorkspaceChanged(boolean value) {
        editedCommand.getApplicableContext().setWorkspaceApplicable(value);

        notifyDirtyStateChanged();
    }
}
