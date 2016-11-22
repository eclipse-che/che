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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Empty status that can render any widget
 */
public class StatusWidget<T extends Widget> implements EmptyStatus<T> {


    private final IsWidget     widget;
    private       Predicate<T> showPredicate;
    private       T            parent;

    public StatusWidget(IsWidget widget) {
        this.widget = widget;
    }

    @Override
    public void paint() {
        if (showPredicate.apply(parent)) {
            parent.getElement().appendChild(widget.asWidget().getElement());
        }
    }

    @Override
    public void init(T widget, Predicate<T> showPredicate) {
        parent = widget;
        this.showPredicate = showPredicate;
    }

}
