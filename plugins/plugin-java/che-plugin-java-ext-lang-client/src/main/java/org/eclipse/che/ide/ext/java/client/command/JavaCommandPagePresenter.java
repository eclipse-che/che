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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.client.command.mainclass.SelectNodePresenter;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;

import javax.validation.constraints.NotNull;

/**
 * Page allows to configure Java command parameters.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaCommandPagePresenter implements JavaCommandPageView.ActionDelegate, CommandConfigurationPage<JavaCommandConfiguration> {
    private final JavaCommandPageView view;
    private final SelectNodePresenter selectNodePresenter;
    private final AppContext appContext;

    private JavaCommandConfiguration editedConfiguration;
    private String                   originCommandLine;
    private String                   originMainClass;
    private DirtyStateListener       listener;
    private FieldStateActionDelegate delegate;

    @Inject
    public JavaCommandPagePresenter(JavaCommandPageView view,
                                    SelectNodePresenter selectNodePresenter,
                                    AppContext appContext) {
        this.view = view;
        this.selectNodePresenter = selectNodePresenter;
        this.appContext = appContext;
        view.setDelegate(this);
    }

    @Override
    public void resetFrom(JavaCommandConfiguration configuration) {
        editedConfiguration = configuration;
        originCommandLine = configuration.getCommandLine();
        originMainClass = configuration.getMainClass();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setMainClass(editedConfiguration.getMainClass());
        view.setCommandLine(editedConfiguration.getCommandLine());

        delegate.updatePreviewURLState(false);
    }

    @Override
    public boolean isDirty() {
        return !originCommandLine.equals(editedConfiguration.getCommandLine()) ||
               !originMainClass.equals(editedConfiguration.getMainClass());
    }

    @Override
    public void setDirtyStateListener(@NotNull DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void setFieldStateActionDelegate(FieldStateActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onAddMainClassBtnClicked() {
        selectNodePresenter.show(this);
    }

    @Override
    public void onCommandLineChanged() {
        editedConfiguration.setCommandLine(view.getCommandLine());
        listener.onDirtyStateChanged();
    }

    public void setMainClass(String path, String fqn) {
        if (editedConfiguration.getMainClass().equals(path)) {
            return;
        }

        String projectPath = appContext.getCurrentProject().getProjectConfig().getPath();
        String relativePath = path.substring(path.indexOf(projectPath) + projectPath.length() + 1);

        view.setMainClass(relativePath);
        editedConfiguration.setCommandLine(editedConfiguration.getCommandLine().replace(editedConfiguration.getMainClass(),relativePath));
        editedConfiguration.setCommandLine(editedConfiguration.getCommandLine().replace(editedConfiguration.getMainClassFqn(),fqn));
        editedConfiguration.setMainClass(relativePath);
        listener.onDirtyStateChanged();
    }

    public JavaCommandConfiguration getConfiguration() {
        return editedConfiguration;
    }
}
