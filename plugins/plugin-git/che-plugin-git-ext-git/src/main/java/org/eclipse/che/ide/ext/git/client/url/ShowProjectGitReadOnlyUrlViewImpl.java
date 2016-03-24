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
package org.eclipse.che.ide.ext.git.client.url;

import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The implementation of View.
 *
 * @author Andrey Plotnikov
 * @author Oleksii Orel
 */
@Singleton
public class ShowProjectGitReadOnlyUrlViewImpl extends Window implements ShowProjectGitReadOnlyUrlView {
    interface ShowProjectGitReadOnlyUrlViewImplUiBinder extends UiBinder<Widget, ShowProjectGitReadOnlyUrlViewImpl> {
    }

    private static ShowProjectGitReadOnlyUrlViewImplUiBinder ourUiBinder = GWT.create(ShowProjectGitReadOnlyUrlViewImplUiBinder.class);

    @UiField
    TextBox   localUrl;
    @UiField
    FlowPanel remotePanel;

    Button btnClose;
    @UiField(provided = true)
    final GitResources            res;
    @UiField(provided = true)
    final GitLocalizationConstant locale;

    private final ClipboardButtonBuilder buttonBuilder;
    private       ActionDelegate         delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param locale
     */
    @Inject
    protected ShowProjectGitReadOnlyUrlViewImpl(GitResources resources, GitLocalizationConstant locale,
                                                ClipboardButtonBuilder buttonBuilder) {
        this.res = resources;
        this.locale = locale;
        this.buttonBuilder = buttonBuilder;
        this.ensureDebugId("projectReadOnlyGitUrl-window");

        Widget widget = ourUiBinder.createAndBindUi(this);

        this.setTitle(locale.projectReadOnlyGitUrlWindowTitle());
        this.setWidget(widget);

        btnClose = createButton(locale.buttonClose(), "", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onCloseClicked();
            }
        });
        btnClose.ensureDebugId("projectReadOnlyGitUrl-btnClose");
        addButtonToFooter(btnClose);

        buttonBuilder.withResourceWidget(localUrl).build();
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(btnClose)) {
            delegate.onCloseClicked();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setLocaleUrl(@NotNull String url) {
        localUrl.setText(url);
    }

    @Override
    public void setRemotes(List<Remote> remotes) {
        remotePanel.clear();
        if (remotes == null || remotes.size() == 0) {
            return;
        }
        remotePanel.add(new Label(
                remotes.size() > 1 ? locale.projectReadOnlyGitRemoteUrlsTitle() : locale.projectReadOnlyGitRemoteUrlTitle()));
        for (Remote remote : remotes) {
            if (remote == null || remote.getUrl() == null) {
                continue;
            }
            TextBox remoteUrl = new TextBox();
            remoteUrl.setReadOnly(true);
            remoteUrl.setText(remote.getUrl());
            remotePanel.add(remoteUrl);
            buttonBuilder.withResourceWidget(remoteUrl).build();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show(btnClose);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
    }
}
