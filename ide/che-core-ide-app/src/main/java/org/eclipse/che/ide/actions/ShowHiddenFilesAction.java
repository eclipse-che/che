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
package org.eclipse.che.ide.actions;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;

import javax.validation.constraints.NotNull;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for showing/hiding hidden files.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
public class ShowHiddenFilesAction extends AbstractPerspectiveAction implements PromisableAction {

    public static final String SHOW_HIDDEN_FILES_PARAM_ID = "showHiddenFiles";
    private final AppContext               appContext;
    private final ProjectExplorerPresenter projectExplorerPresenter;

    @Inject
    public ShowHiddenFilesAction(AppContext appContext,
                                 CoreLocalizationConstant localizationConstant,
                                 ProjectExplorerPresenter projectExplorerPresenter,
                                 Resources resources) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.actionShowHiddenFilesTitle(),
              localizationConstant.actionShowHiddenFilesDescription(),
              null,
              resources.showHiddenFiles());
        this.appContext = appContext;
        this.projectExplorerPresenter = projectExplorerPresenter;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(appContext.getRootProject() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isShow = projectExplorerPresenter.isShowHiddenFiles();
        projectExplorerPresenter.showHiddenFiles(!isShow);
    }

    @Override
    public Promise<Void> promise(final ActionEvent event) {
        if (event.getParameters() == null || event.getParameters().get(SHOW_HIDDEN_FILES_PARAM_ID) == null) {
            return Promises.reject(JsPromiseError.create("Mandatory parameter" + SHOW_HIDDEN_FILES_PARAM_ID + " is not specified"));
        }

        final String showHiddenFilesKey = event.getParameters().get(SHOW_HIDDEN_FILES_PARAM_ID);
        final boolean isShowHiddenFiles = Boolean.valueOf(showHiddenFilesKey);

        final CallbackPromiseHelper.Call<Void, Throwable> call = new CallbackPromiseHelper.Call<Void, Throwable>() {

            @Override
            public void makeCall(final Callback<Void, Throwable> callback) {
                projectExplorerPresenter.showHiddenFiles(isShowHiddenFiles);

                callback.onSuccess(null);
            }
        };

        return createFromCallback(call);
    }
}
