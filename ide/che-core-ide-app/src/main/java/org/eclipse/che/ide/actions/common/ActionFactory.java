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
package org.eclipse.che.ide.actions.common;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.data.tree.TreeExpander;

/**
 * Utility methods to create basic common actions.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Beta
public class ActionFactory {

    /**
     * Returns the instance of the {@link TreeExpandAction} based on the given {@code expander}.
     *
     * @param expander
     *         tree expander
     * @return instance of the {@link TreeExpandAction}
     * @see TreeExpandAction
     * @see TreeExpander
     * @since 5.0.0
     */
    public Action createExpandTreeAction(final TreeExpander expander) {
        return new TreeExpandAction() {
            @Override
            public TreeExpander getTreeExpander() {
                return expander;
            }
        };
    }

    /**
     * Returns the instance of the {@link TreeCollapseAction} based on the given {@code expander}.
     *
     * @param expander
     *         tree expander
     * @return instance of the {@link TreeCollapseAction}
     * @see TreeCollapseAction
     * @see TreeExpander
     * @since 5.0.0
     */
    public Action createCollapseTreeAction(final TreeExpander expander) {
        return new TreeCollapseAction() {
            @Override
            public TreeExpander getTreeExpander() {
                return expander;
            }
        };
    }

}
