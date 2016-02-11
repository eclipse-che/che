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
package org.eclipse.che.ide.navigation;

import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.ide.api.mvp.View;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * View for file navigation (find file by name and open it).
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 */
public interface NavigateToFileView extends View<NavigateToFileView.ActionDelegate> {
    /** Needs for delegate some function into NavigateToFile view. */
    public interface ActionDelegate {
        /**
         * Called when suggestions are requested.
         *
         * @param query
         *         query string
         * @param callback
         *         callback
         */
        void onRequestSuggestions(String query, AsyncCallback<List<ItemReference>> callback);

        /** Called when file selected. */
        void onFileSelected();
    }

    /**
     * Returns chosen item's path.
     *
     * @return chosen item's path
     */
    String getItemPath();

    /** Clear input. */
    void clearInput();

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}
