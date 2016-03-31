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
package org.eclipse.che.plugin.svn.ide.commit.diff;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link DiffViewerView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class DiffViewerViewImpl extends Window implements DiffViewerView {
    interface DiffViewerViewImplUiBinder extends UiBinder<Widget, DiffViewerViewImpl> {
    }

    private static DiffViewerViewImplUiBinder uiBinder = GWT.create(DiffViewerViewImplUiBinder.class);

    private DiffViewerView.ActionDelegate delegate;

    @UiField
    RichTextArea diffViewer;

    @Inject
    public DiffViewerViewImpl(SubversionExtensionLocalizationConstants locale) {
        this.setTitle(locale.commitTitle());
        this.setWidget(uiBinder.createAndBindUi(this));

        Button btnClose = createButton("Close", "svn-diff-view-close", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onCloseClicked();
            }
        });

        getFooter().add(btnClose);

        diffViewer.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public void setDiffContent(String content) {
        diffViewer.setHTML(content);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onShow() {
        show();
    }
}
