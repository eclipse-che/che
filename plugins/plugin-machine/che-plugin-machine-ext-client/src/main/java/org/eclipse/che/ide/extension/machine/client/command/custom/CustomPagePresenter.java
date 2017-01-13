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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Page allows to edit arbitrary command.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomPagePresenter implements CustomPageView.ActionDelegate, CommandPage {

    private final CustomPageView view;

    private CommandImpl editedCommand;

    // initial value of the 'command line' parameter
    private String commandLineInitial;

    private DirtyStateListener listener;

    @Inject
    public CustomPagePresenter(CustomPageView view) {
        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public void resetFrom(CommandImpl command) {
        editedCommand = command;
        commandLineInitial = command.getCommandLine();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setCommandLine(editedCommand.getCommandLine());
    }

    @Override
    public void onSave() {
        commandLineInitial = editedCommand.getCommandLine();
    }

    @Override
    public boolean isDirty() {
        return !commandLineInitial.equals(editedCommand.getCommandLine());
    }

    @Override
    public void setDirtyStateListener(DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {
    }

    @Override
    public void onCommandLineChanged() {
        editedCommand.setCommandLine(view.getCommandLine());

        listener.onDirtyStateChanged();
    }
}
