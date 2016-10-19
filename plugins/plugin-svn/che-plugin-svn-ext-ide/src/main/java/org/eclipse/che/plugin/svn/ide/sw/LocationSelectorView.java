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
package org.eclipse.che.plugin.svn.ide.sw;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The dialog to select custom location to switch.
 *
 * @author Anatolii Bazko
 */
public interface LocationSelectorView extends View<LocationSelectorView.ActionDelegate> {

    interface ActionDelegate {
        void setSelectedNode(SvnNode node);
    }

    /** Show the view. */
    void showWindow();

    /**
     * Initialize tree with root node.
     */
    void setRootNode(SvnNode rootNode);
}
