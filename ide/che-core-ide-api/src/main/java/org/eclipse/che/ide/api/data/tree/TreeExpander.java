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
package org.eclipse.che.ide.api.data.tree;

import com.google.common.annotations.Beta;

/**
 * Component which performs basic tree operation such as expand and collapse.
 *
 * @author Vlad Zhukovskyi
 * @since 5.0.0
 */
@Beta
public interface TreeExpander {

    /**
     * Perform tree expand in case if {@link #isExpandEnabled()} returns {@code true}.
     */
    void expandTree();

    /**
     * Returns {@code true} in case if tree expand is possible.
     *
     * @return {@code true} in case if tree expand is possible, otherwise {@code false}
     */
    boolean isExpandEnabled();

    /**
     * Perform tree collapse in case if {@link #isCollapseEnabled()} returns {@code true}.
     */
    void collapseTree();

    /**
     * Returns {@code true} in case if tree collapse is possible.
     *
     * @return {@code true} in case if tree collapse is possible, otherwise {@code false}
     */
    boolean isCollapseEnabled();
}
