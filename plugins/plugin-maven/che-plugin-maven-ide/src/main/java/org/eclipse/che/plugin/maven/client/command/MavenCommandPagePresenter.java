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
package org.eclipse.che.plugin.maven.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Page allows to customize Maven command.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenCommandPagePresenter implements MavenCommandPageView.ActionDelegate, CommandPage {

    private final MavenCommandPageView view;

    private CommandImpl       editedCommand;
    private MavenCommandModel editedCommandModel;

    // initial value of the 'working directory' parameter
    private String workingDirectoryInitial;
    // initial value of the 'arguments' parameter
    private String argumentsInitial;

    private DirtyStateListener listener;

    @Inject
    public MavenCommandPagePresenter(MavenCommandPageView view) {
        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public void resetFrom(CommandImpl command) {
        editedCommand = command;

        editedCommandModel = MavenCommandModel.fromCommandLine(command.getCommandLine());

        workingDirectoryInitial = editedCommandModel.getWorkingDirectory();
        argumentsInitial = editedCommandModel.getArguments();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setWorkingDirectory(editedCommandModel.getWorkingDirectory());
        view.setArguments(editedCommandModel.getArguments());
    }

    @Override
    public void onSave() {
        workingDirectoryInitial = editedCommandModel.getWorkingDirectory();
        argumentsInitial = editedCommandModel.getArguments();
    }

    @Override
    public boolean isDirty() {
        return !(workingDirectoryInitial.equals(editedCommandModel.getWorkingDirectory()) &&
                 argumentsInitial.equals(editedCommandModel.getArguments()));
    }

    @Override
    public void setDirtyStateListener(DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {
    }

    @Override
    public void onWorkingDirectoryChanged() {
        editedCommandModel.setWorkingDirectory(view.getWorkingDirectory());

        editedCommand.setCommandLine(editedCommandModel.toCommandLine());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onArgumentsChanged() {
        editedCommandModel.setArguments(view.getArguments());

        editedCommand.setCommandLine(editedCommandModel.toCommandLine());
        listener.onDirtyStateChanged();
    }
}
