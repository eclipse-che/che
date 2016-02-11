/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/** @author Vladyslav Zhukovskii */
public class AntPageViewImpl implements AntPageView {
    private static AntPageViewImplUiBinder ourUiBinder = GWT.create(AntPageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;

    interface AntPageViewImplUiBinder extends UiBinder<DockLayoutPanel, AntPageViewImpl> {
    }

    /** Create instance of {@link AntPageViewImpl}. */
    public AntPageViewImpl() {
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return rootElement;
    }
}
