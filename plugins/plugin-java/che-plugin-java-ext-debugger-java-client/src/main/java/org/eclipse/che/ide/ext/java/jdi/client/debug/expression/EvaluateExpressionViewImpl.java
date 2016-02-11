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
package org.eclipse.che.ide.ext.java.jdi.client.debug.expression;

import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;


/**
 * The implementation of {@link EvaluateExpressionView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class EvaluateExpressionViewImpl extends DialogBox implements EvaluateExpressionView {
    interface EvaluateExpressionViewImplUiBinder extends UiBinder<Widget, EvaluateExpressionViewImpl> {
    }

    private static EvaluateExpressionViewImplUiBinder ourUiBinder = GWT.create(EvaluateExpressionViewImplUiBinder.class);

    @UiField
    Button                          btnEvaluate;
    @UiField
    Button                          btnCancel;
    @UiField
    TextBox                         expression;
    @UiField
    TextArea                        result;
    @UiField(provided = true)
    JavaRuntimeLocalizationConstant locale;
    @UiField(provided = true)
    JavaRuntimeResources            res;
    private ActionDelegate delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param locale
     */
    @Inject
    protected EvaluateExpressionViewImpl(JavaRuntimeResources resources, JavaRuntimeLocalizationConstant locale) {
        this.locale = locale;
        this.res = resources;

        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setText(this.locale.evaluateExpressionViewTitle());
        this.setWidget(widget);

        expression.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                    delegate.onEvaluateClicked();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getExpression() {
        return expression.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setExpression(@NotNull String expression) {
        this.expression.setText(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void setResult(@NotNull String value) {
        this.result.setText(value);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableEvaluateButton(boolean enabled) {
        btnEvaluate.setEnabled(enabled);
    }

    /** {@inheritDoc} */
    @Override
    public void focusInExpressionField() {
        expression.setFocus(true);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.center();
        this.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("btnEvaluate")
    public void onEvaluateClicked(ClickEvent event) {
        delegate.onEvaluateClicked();
    }

    @UiHandler("btnCancel")
    public void onCancelClicked(ClickEvent event) {
        delegate.onCancelClicked();
    }

    @UiHandler("expression")
    public void handleKeyUp(KeyUpEvent event) {
        delegate.onExpressionValueChanged();
    }
}