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
package org.eclipse.che.ide.search.presentation;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ui.smartTree.Tree;

import java.util.List;

/**
 * View for the result of search.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(FindResultViewImpl.class)
public interface FindResultView extends View<FindResultView.ActionDelegate> {
    /**
     * Sets whether this panel is visible.
     *
     * @param visible
     *         visible - true to show the object, false to hide it
     */
    void setVisible(boolean visible);

    /**
     * Activate Find results part and showing all occurrences.
     *
     * @param nodes
     *         list of files which contains requested text
     * @param request
     *         requested text
     */
    void showResults(Resource[] resources, String request);

    Tree getTree();

    interface ActionDelegate extends BaseActionDelegate {
        void onSelectionChanged(List<Node> selection);
    }

}
