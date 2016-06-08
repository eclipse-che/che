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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.che.ide.ui.window.Window;


public class TestRunnerViewImpl extends Window implements TestRunnerView {

    interface TestRunnerViewImplUiBinder extends UiBinder<Widget, TestRunnerViewImpl> {
    }

    private static TestRunnerViewImplUiBinder uiBinder = GWT.create(TestRunnerViewImplUiBinder.class);


    private ActionDelegate delegate;

    private TestResult lastTestResult;

    Button btnRunTest;
    Button btnRunAllTest;
    Button btnGotoError;
    Button btnCancel;

    @UiField
    TextArea reference;

    @UiField
    Label failureCount;

    @UiField
    Label testFramework;

    @UiField
    Label success;

    @UiField
    ListBox listBox;

    @Inject
    public TestRunnerViewImpl() {

        Widget widget = uiBinder.createAndBindUi(this);

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


        btnRunAllTest = createButton("Run All", "test-runner-run-all", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onRunAllClicked();
            }
        });
        addButtonToFooter(btnRunAllTest);

        btnGotoError = createButton("Goto Error", "test-runner-goto-error", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onGotoError();
            }
        });
        addButtonToFooter(btnGotoError);

        testFramework.setText("Test Framework: ");
        failureCount.setText("Failure Count: ");
        success.setText("Success: ");
    }

    @Override
    public void showDialog() {
        reference.setReadOnly(true);
        reference.setVisibleLines(21);
        reference.getElement().setAttribute("wrap", "off");
        this.show();
//        new Timer() {
//            @Override
//            public void run() {
//                reference.setFocus(true);
//            }
//        }.schedule(300);
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
    public void setTestResult(TestResult result) {
        lastTestResult = result;
        updateUI();
    }

    @Override
    public Failure getSelectedFailure() {
        int selectedIndex = listBox.getSelectedIndex();
        if(selectedIndex >= 0 || selectedIndex < lastTestResult.getFailures().size()) {
            return lastTestResult.getFailures().get(selectedIndex);
        }
        return null;
    }

    private void updateUI(){
        testFramework.setText("Test Framework: " + lastTestResult.getTestFramework());
        failureCount.setText("Failure Count: " + Integer.toString(lastTestResult.getFailureCount()));
        success.setText("Success: " + (lastTestResult.isSuccess() ? "Yes" : "No"));
        listBox.clear();
        for(Failure failure : lastTestResult.getFailures()){
            listBox.addItem(failure.getMessage() + " in class: " + failure.getFailingClass()
                    + "(" + failure.getFailingMethod() + ")");
        }

        listBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                Failure selectedFailure = lastTestResult.getFailures().get(listBox.getSelectedIndex());
                String message = "Failing class: " + selectedFailure.getFailingClass()
                        + "\nFailing method: " + selectedFailure.getFailingMethod()
                        + "\nAssertion error: " + selectedFailure.getMessage()
                        + "\n\nStacktrace:\n" + selectedFailure.getTrace();
                reference.setText(message);
            }
        });
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
