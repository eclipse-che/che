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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Page allows to customize command of {@link GwtCheCommandType}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCheCommandPagePresenter implements GwtCheCommandPageView.ActionDelegate, CommandPage {

    private final GwtCheCommandPageView view;

    private CommandImpl        editedCommand;
    private GwtCheCommandModel editedCommandModel;

    // initial value of the 'GWT module' parameter
    private String gwtModuleInitial;
    // initial value of the 'Code Server address' parameter
    private String codeServerAddressInitial;
    // initial value of the 'Classpath' parameter
    private String classPathInitial;

    private DirtyStateListener listener;

    @Inject
    public GwtCheCommandPagePresenter(GwtCheCommandPageView view) {
        this.view = view;

        view.setDelegate(this);
    }

    @Override
    public void resetFrom(CommandImpl command) {
        editedCommand = command;

        editedCommandModel = GwtCheCommandModel.fromCommandLine(command.getCommandLine());

        gwtModuleInitial = editedCommandModel.getGwtModule();
        codeServerAddressInitial = editedCommandModel.getCodeServerAddress();
        classPathInitial = editedCommandModel.getClassPath();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setGwtModule(editedCommandModel.getGwtModule());
        view.setCodeServerAddress(editedCommandModel.getCodeServerAddress());
        view.setClassPath(editedCommandModel.getClassPath());
    }

    @Override
    public void onSave() {
        gwtModuleInitial = editedCommandModel.getGwtModule();
        codeServerAddressInitial = editedCommandModel.getCodeServerAddress();
        classPathInitial = editedCommandModel.getClassPath();
    }

    @Override
    public boolean isDirty() {
        return !(gwtModuleInitial.equals(editedCommandModel.getGwtModule()) &&
                 codeServerAddressInitial.equals(editedCommandModel.getCodeServerAddress()) &&
                 classPathInitial.equals(editedCommandModel.getClassPath()));
    }

    @Override
    public void setDirtyStateListener(DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {
    }

    @Override
    public void onGwtModuleChanged() {
        editedCommandModel.setGwtModule(view.getGwtModule());

        editedCommand.setCommandLine(editedCommandModel.toCommandLine());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onCodeServerAddressChanged() {
        editedCommandModel.setCodeServerAddress(view.getCodeServerAddress());

        editedCommand.setCommandLine(editedCommandModel.toCommandLine());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onClassPathChanged() {
        editedCommandModel.setClassPath(view.getClassPath());

        editedCommand.setCommandLine(editedCommandModel.toCommandLine());
        listener.onDirtyStateChanged();
    }
}
