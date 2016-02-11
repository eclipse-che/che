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

import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;

/**
 * @author Andrienko Alexander
 */
@Singleton
public class ExpandNodeAction extends Action implements PromisableAction {

    /** ID of the parameter to specify node path to open. */
    public static final String NODE_PARAM_ID = "node";

    private final AppContext               appContext;
    private final CoreLocalizationConstant localization;
    private final ProjectExplorerPresenter projectExplorer;

    private Callback<Void, Throwable> actionCompletedCallBack;

    @Inject
    public ExpandNodeAction(AppContext appContext,
                            CoreLocalizationConstant localization,
                            ProjectExplorerPresenter projectExplorer) {
        this.appContext = appContext;
        this.localization = localization;
        this.projectExplorer = projectExplorer;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null || currentProject.getRootProject() == null) {
            return;
        }

        if (event.getParameters() == null) {
            Log.error(getClass(), localization.canNotOpenNodeWithoutParams());
            return;
        }

        final String path = event.getParameters().get(NODE_PARAM_ID);

        if (Strings.isNullOrEmpty(path)) {
            Log.error(getClass(), localization.nodeToOpenIsNotSpecified());
            return;
        }

        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(path)).then(new Operation<Node>() {
            private HandlerRegistration handlerRegistration;

            @Override
            public void apply(final Node arg) throws OperationException {
                if (!projectExplorer.isExpanded(arg)) {
                    handlerRegistration = projectExplorer.addExpandHandler(new ExpandNodeEvent.ExpandNodeHandler() {
                        @Override
                        public void onExpand(ExpandNodeEvent event) {
                            if (((HasStorablePath)event.getNode()).getStorablePath().equals(((HasStorablePath)arg).getStorablePath())) {
                                handlerRegistration.removeHandler();
                                if (actionCompletedCallBack != null) {
                                    actionCompletedCallBack.onSuccess(null);
                                }
                            }
                        }
                    });

                    projectExplorer.setExpanded(arg, true);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (actionCompletedCallBack != null) {
                    actionCompletedCallBack.onFailure(arg.getCause());
                }
            }
        });
    }

    @Override
    public Promise<Void> promise(final ActionEvent event) {
        final CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null) {
            return Promises.reject(JsPromiseError.create(localization.noOpenedProject()));
        }

        final Call<Void, Throwable> call = new Call<Void, Throwable>() {
            @Override
            public void makeCall(final Callback<Void, Throwable> callback) {
                actionCompletedCallBack = callback;
                actionPerformed(event);
            }
        };

        return createFromCallback(call);
    }

}
