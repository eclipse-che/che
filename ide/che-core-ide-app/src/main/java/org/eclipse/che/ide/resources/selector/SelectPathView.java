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
package org.eclipse.che.ide.resources.selector;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

import java.util.List;

/**
 * @author Vlad Zhukovskiy
 */
@ImplementedBy(SelectPathViewImpl.class)
public interface SelectPathView extends View<SelectPathView.ActionDelegate> {

    void setStructure(List<Node> nodes, boolean showFiles);

    void show();

    interface ActionDelegate {
        void onPathSelected(Path path);

        void onSubmit();

        void onCancel();
    }
}