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

import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

/**
 * View of {@link LoaderPresenter}.
 *
 * @author Roman Nikitenko
 */
public interface LoaderView extends IsWidget {
    interface ActionDelegate {
        /**
         * Performs any actions appropriate in response to the user having clicked the expander area.
         */
        void onExpanderClicked();
    }
    /** Sets the delegate to receive events from this view. */
    void setDelegate(ActionDelegate delegate);

    /** Sets the list of operations for displaying. */
    void setOperations(List<String> operations);

    /** Sets the current operation for displaying. */
    void setCurrentOperation(String operation);

    /** Sets the 'error' status for operation with {@code index}. */
    void setErrorStatus(int index, String operation);

    /** Sets the 'success' status for operation with {@code index}. */
    void setSuccessStatus(int index, String operation);

    /** Sets the 'in progress' status for operation with {@code index}. */
    void setInProgressStatus(int index, String operation);

    /** Expand Operations area. */
    void expandOperations();

    /** Collapse Operations area. */
    void collapseOperations();

    /** Displays the progress bar's state corresponding to {@code percent}. */
    void setProgressBarState(int percent);
}
