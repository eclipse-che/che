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
package org.eclipse.che.plugin.embedjsexample.ide.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.plugin.embedjsexample.ide.view.client.jso.HelloWorldViewOverlay;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
public class HelloWorldViewImpl extends BaseView<HelloWorldView.ActionDelegate> implements HelloWorldView {

    interface HelloWorldViewImplUiBinder extends UiBinder<Widget, HelloWorldViewImpl> {
    }

    private final static HelloWorldViewImplUiBinder UI_BINDER = GWT.create(HelloWorldViewImplUiBinder.class);

    @UiField
    FlowPanel helloWorldPanel;

    @Inject
    public HelloWorldViewImpl(PartStackUIResources resources) {
        super(resources);
        setContentWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void sayHello(String content) {
        HelloWorldViewOverlay.sayHello(helloWorldPanel.getElement(), content);
        helloWorldPanel.setVisible(true);
    }

}
