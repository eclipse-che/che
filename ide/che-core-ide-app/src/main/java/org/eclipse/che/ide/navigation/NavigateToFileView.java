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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

import java.util.List;

/**
 * View for file navigation (find file by name and open it).
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@ImplementedBy(NavigateToFileViewImpl.class)
public interface NavigateToFileView extends View<NavigateToFileView.ActionDelegate> {
    /** Needs for delegate some function into NavigateToFile view. */
    interface ActionDelegate {
        /**
         * Called when suggestions are requested.
         *
         * @param query
         *         query string
         * @param callback
         *         callback
         */
        void onRequestSuggestions(String query, AsyncCallback<List<Path>> callback);

        /** Called when file selected. */
        void onFileSelected(Path path);
    }

    /** Clear input. */
    void clearInput();

    /** Close dialog. */
    void close();

    /** Show dialog. */
    void showDialog();
}
