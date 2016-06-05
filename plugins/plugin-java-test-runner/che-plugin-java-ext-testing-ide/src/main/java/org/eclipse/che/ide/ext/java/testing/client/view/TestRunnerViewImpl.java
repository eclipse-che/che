/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ui.window.Window;


public class TestRunnerViewImpl extends Window implements TestRunnerView {

    interface CheckoutReferenceViewImplUiBinder extends UiBinder<Widget, TestRunnerViewImpl> {
    }

    private static CheckoutReferenceViewImplUiBinder ourUiBinder = GWT.create(CheckoutReferenceViewImplUiBinder.class);


    private ActionDelegate delegate;

    Button btnRunTest;
    Button btnCancel;
    @UiField
    TextArea reference;

    @Inject
    public TestRunnerViewImpl() {

        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setTitle("Test Runner");
        this.setWidget(widget);

        btnCancel = createButton("Cancel", "test-runner-cancel", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCancelClicked();
            }
        });
        addButtonToFooter(btnCancel);

        btnRunTest = createButton("Run", "test-runner-run", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onRunClicked();
            }
        });
        addButtonToFooter(btnRunTest);
    }

    @Override
    public void showDialog() {
        reference.setText("Waiting for run the tests...");
        reference.setReadOnly(true);
        reference.setVisibleLines(10);
        this.show();
//        reference.setReadOnly(true);
        new Timer() {
            @Override
            public void run() {
                reference.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void close() {
        this.hide();
    }

    @Override
    public void setText(String message) {
        reference.setValue(message);
    }


    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onEnterClicked() {
        delegate.onEnterClicked();
    }
}
