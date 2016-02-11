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
package org.eclipse.che.ide.search;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.search.selectpath.SelectPathPresenter;
import org.eclipse.che.ide.ui.window.Window;

/**
 * The implementation of {@link FullTextSearchView} view.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FullTextSearchViewImpl extends Window implements FullTextSearchView {

    interface FullTextSearchViewImplUiBinder extends UiBinder<Widget, FullTextSearchViewImpl> {
    }

    @UiField
    Label                    errLabel;
    @UiField(provided = true)
    CoreLocalizationConstant locale;
    private final AppContext appContext;
    @UiField
    TextBox  text;
    @UiField
    TextBox  filesMask;
    @UiField
    CheckBox isUseFileMask;
    @UiField
    CheckBox isUseDirectory;
    @UiField
    TextBox  directory;
    @UiField
    Button   selectPathButton;

    private ActionDelegate delegate;
    private Button         accept;

    @Inject
    public FullTextSearchViewImpl(CoreLocalizationConstant locale,
                                  final SelectPathPresenter selectPathPresenter,
                                  FullTextSearchViewImplUiBinder uiBinder,
                                  AppContext appContext) {
        this.locale = locale;
        this.appContext = appContext;

        setTitle(locale.textSearchTitle());

        Widget widget = uiBinder.createAndBindUi(this);
        setWidget(widget);

        createButtons();
        addHandlers(selectPathPresenter);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void showDialog() {
        accept.setEnabled(false);
        isUseFileMask.setValue(false);
        filesMask.setEnabled(false);
        isUseDirectory.setValue(false);
        directory.setEnabled(false);
        selectPathButton.setEnabled(false);
        directory.setText(appContext.getCurrentProject().getRootProject().getPath());
        filesMask.setText("*.*");
        directory.setText("/");
        errLabel.setText("");

        new Timer() {
            @Override
            public void run() {
                text.setFocus(true);
            }
        }.schedule(100);

        super.show();
    }

    @Override
    public void setPathDirectory(String path) {
        directory.setText(path);
    }

    @Override
    public String getFileMask() {
        return isUseFileMask.getValue() ? filesMask.getText() : "";
    }

    @Override
    public String getPathToSearch() {
        return isUseDirectory.getValue() ? directory.getText() : "";
    }

    @Override
    public void showErrorMessage(String message) {
        errLabel.setText(message);
    }

    @Override
    protected void onClose() {
        //Do nothing
    }

    @Override
    protected void onEnterClicked() {
        if (!text.getText().isEmpty()) {
            delegate.search(text.getText());
        }
    }

    @Override
    public void clearInput() {
        text.setText("");
    }

    @Override
    public void setFocus() {
        accept.setFocus(true);
    }

    private void createButtons() {
        Button cancel = createButton(locale.cancel(), "search-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        accept = createPrimaryButton(locale.search(), "search-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.search(text.getText());
            }
        });

        addButtonToFooter(accept);
        addButtonToFooter(cancel);
    }

    private void addHandlers(final SelectPathPresenter selectPathPresenter) {
        isUseFileMask.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                filesMask.setEnabled(event.getValue());
            }
        });

        isUseDirectory.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                directory.setEnabled(event.getValue());
                selectPathButton.setEnabled(event.getValue());
            }
        });

        text.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                accept.setEnabled(!text.getValue().isEmpty());
            }
        });

        selectPathButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectPathPresenter.show(delegate);
            }
        });
    }

}
