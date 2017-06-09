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
package org.eclipse.che.ide.navigation;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.project.shared.dto.ItemReference;
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

    /**
     * Is needed to delegate actions to corresponding presenter.
     */
    interface ActionDelegate {

        /**
         * Is called when file name is changed.
         *
         * @param fileName
         *          file name
         */
        void onFileNameChanged(String fileName);

        /**
         * Is called when file is selected.
         *
         * @param path
         *          file path
         */
        void onFileSelected(Path path);

    }

    /**
     * Show popup.
     */
    void showPopup();

    /**
     * Hide popup.
     */
    void hidePopup();

    /**
     * Show suggestion popup with list of items.
     *
     * @param items
     *      items of suggestions
     */
    void showItems(List<ItemReference> items);

}
