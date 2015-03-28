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
package com.codenvy.ide.tutorial.parts.part;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * The implementation of {@link MyPartView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class MyPartViewImpl extends BaseView<MyPartView.ActionDelegate> implements MyPartView {

    interface MyPartViewImplUiBinder extends UiBinder<Widget, MyPartViewImpl> {
    }

    @UiField
    Button button;

    @Inject
    public MyPartViewImpl(MyPartViewImplUiBinder uiBinder,
                          PartStackUIResources resources) {
        super(resources);
        setContentWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("button")
    public void onButtonClicked(ClickEvent event) {
        delegate.onButtonClicked();
    }

}
