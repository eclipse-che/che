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
package org.eclipse.che.ide.reference;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

import javax.validation.constraints.NotNull;

/**
 * The class contains business logic which allows us display fqn and path for files, folders, packages nodes etc., and construct the
 * the buttons via {@link ClipboardButtonBuilder}, which allows to store values from text fields to browser clip board.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
final class ShowReferenceViewImpl extends Window implements ShowReferenceView {
    interface ShowReferenceViewImplUiBinder extends UiBinder<Widget, ShowReferenceViewImpl> {
    }

    @UiField(provided = true)
    final CoreLocalizationConstant locale;

    @UiField
    FlowPanel referencePanel;
    @UiField
    TextBox   reference;
    @UiField
    FlowPanel pathPanel;
    @UiField
    TextBox   path;

    @Inject
    public ShowReferenceViewImpl(ShowReferenceViewImplUiBinder binder,
                                 CoreLocalizationConstant locale,
                                 ClipboardButtonBuilder clipBoardBtnBuilder) {
        this.locale = locale;

        setTitle(locale.showReference());

        setWidget(binder.createAndBindUi(this));
        setHideOnEscapeEnabled(true);

        clipBoardBtnBuilder.withResourceWidget(reference).build();
        clipBoardBtnBuilder.withResourceWidget(path).build();

        addButtons();
    }

    private void addButtons() {
        Button cancel = createButton(locale.cancel(), "copy-reference-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        addButtonToFooter(cancel);
    }

    @Override
    public void show(@Nullable String reference, @NotNull String path) {
        boolean hasReference = !Strings.isNullOrEmpty(reference);

        this.reference.setText(hasReference ? reference : "");
        this.referencePanel.setVisible(hasReference);
        this.path.setText(path);

        super.show();

        new Timer() {
            @Override
            public void run() {
                ShowReferenceViewImpl.this.reference.setFocus(true);
            }
        }.schedule(100);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        //to do nothing
    }
}
