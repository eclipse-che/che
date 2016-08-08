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
package org.eclipse.che.ide.ui.loaders.initialization;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.WORKING;
import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.COMPLETED;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.SUCCESS;

/**
 * Loader for displaying information about a process of loading.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class LoaderPresenter implements OperationInfo.StatusListener, LoaderView.ActionDelegate {

    private final LoaderView view;

    LoaderStateListener listener;
    List<OperationInfo> operations;
    boolean             expandPanelState;

    @Inject
    public LoaderPresenter(LoaderView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    /**
     * @return custom Widget that represents the loader's action in UI.
     */
    public Widget getCustomComponent() {
        return view.asWidget();
    }

    /**
     * Displays information about a process of loading.
     *
     * @param loadingInfo
     *         the object which contains information about operations of loading
     */
    public void show(LoadingInfo loadingInfo) {
        operations = loadingInfo.getOperations();
        List<String> displayNames = loadingInfo.getDisplayNames();

        expandPanelState = false;
        view.collapseOperations();
        view.setOperations(displayNames);
        updateState();

        if (listener != null) {
            listener.onLoaderStateChanged(WORKING);
        }
    }

    /** Hide the loader. */
    public void hide() {
        if (listener == null) {
            return;
        }
        listener.onLoaderStateChanged(COMPLETED);
    }

    @Override
    public void onExpanderClicked() {
        if (expandPanelState) {
            view.collapseOperations();
        } else {
            view.expandOperations();
        }
        expandPanelState = !expandPanelState;
    }

    @Override
    public void onStatusChanged(OperationInfo operation) {
        int operationIndex = operations.indexOf(operation);
        String operationName = operation.getOperationName();

        switch (operation.getStatus()) {
            case IN_PROGRESS:
                view.setInProgressStatus(operationIndex, operationName);
                updateState();
                break;
            case SUCCESS:
                view.setSuccessStatus(operationIndex, operationName);
                updateState();
                break;
            case ERROR:
                view.setErrorStatus(operationIndex, operationName);
                view.setCurrentOperation("Error while " + operationName);
                break;
        }
    }

    private void updateState() {
        if (operations.size() == 0) {
            return;
        }

        int completedOperations = 0;
        for (OperationInfo operation : operations) {
            Status status = operation.getStatus();
            if (IN_PROGRESS.equals(status)) {
                view.setCurrentOperation(operation.getOperationName());
                continue;
            }
            if (SUCCESS.equals(status)) {
                completedOperations++;
            }
        }

        int completedState = completedOperations * 100 / operations.size();
        view.setProgressBarState(completedState);
    }

    public void setListener(LoaderStateListener listener) {
        this.listener = listener;
    }

    /** Listener that will be called when a loader state changed. */
    public interface LoaderStateListener {
        void onLoaderStateChanged(State state);
    }

    /** State of the loader. */
    public enum State {
        WORKING(),
        COMPLETED()
    }
}
