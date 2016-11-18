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
package org.eclipse.che.ide.ui.status;

import com.google.common.base.Predicate;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provide empty status message or widget for tree or other ui container like lists.
 */
public interface EmptyStatus<T extends Widget> {
    /**
     * called when need to show empty status
     */
    void paint();

    /**
     * Initialize with parent widget and condition
     * @param widget the widget that has empty state
     * @param showPredicate showing predicate
     */
    void init(T widget, Predicate<T> showPredicate);
}
