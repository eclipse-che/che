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
package org.eclipse.che.plugin.debugger.ide.debug.changevalue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;


/**
 * The implementation of {@link ChangeValueView}.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ChangeValueViewImpl extends Window implements ChangeValueView {
    interface ChangeValueViewImplUiBinder extends UiBinder<Widget, ChangeValueViewImpl> {
    }

    private static ChangeValueViewImplUiBinder uiBinder = GWT.create(ChangeValueViewImplUiBinder.class);

    @UiField
    TextArea value;
    @UiField
    Label    changeValueLabel;

    private ActionDelegate delegate;
    private Button         changeButton;

    /**
     * Create view.
     */
    @Inject
    protected ChangeValueViewImpl(DebuggerLocalizationConstant locale) {
        Widget widget = uiBinder.createAndBindUi(this);

        this.setTitle(locale.changeValueViewTitle());
        this.setWidget(widget);

        Button cancelButton =
                createButton(locale.changeValueViewCancelButtonTitle(), "debugger-change-value-cancel-btn", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        delegate.onCancelClicked();
                    }
                });

        changeButton = createButton(locale.changeValueViewChangeButtonTitle(), "debugger-change-value-change-btn", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                delegate.onChangeClicked();
            }
        });

        addButtonToFooter(cancelButton);
        addButtonToFooter(changeButton);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getValue() {
        return value.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(@NotNull String value) {
        this.value.setText(value);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableChangeButton(boolean isEnable) {
        changeButton.setEnabled(isEnable);
    }

    /** {@inheritDoc} */
    @Override
    public void focusInValueField() {
        new Timer() {
            @Override
            public void run() {
                value.setFocus(true);
            }
        }.schedule(300);
    }

    /** {@inheritDoc} */
    @Override
    public void selectAllText() {
        value.selectAll();
    }

    /** {@inheritDoc} */
    @Override
    public void setValueTitle(@NotNull String title) {
        changeValueLabel.getElement().setInnerHTML(title);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show();
        if (!value.getText().isEmpty()) {
            value.selectAll();
            setEnableChangeButton(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("value")
    public void onValueChanged(KeyUpEvent event) {
        delegate.onVariableValueChanged();
    }
}
