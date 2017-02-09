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
package org.eclipse.che.plugin.svn.ide.commit.diff;

import com.google.common.base.Splitter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.ui.window.Window;

import java.util.HashMap;
import java.util.Map;

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

    private       DiffViewerView.ActionDelegate delegate;
    private final Map<String, String>           lineRules;

    @UiField
    RichTextArea diffViewer;

    @Inject
    public DiffViewerViewImpl(SubversionExtensionLocalizationConstants locale) {
        this.setTitle(locale.commitTitle());
        this.setWidget(uiBinder.createAndBindUi(this));

        lineRules = new HashMap<>();
        lineRules.put("+", Style.getVcsConsoleStagedFilesColor());
        lineRules.put("-", Style.getVcsConsoleErrorColor());
        lineRules.put("@", Style.getVcsConsoleChangesLineNumbersColor());
        lineRules.put("default", Style.getMainFontColor());

        Button btnClose = createButton("Close", "svn-diff-view-close", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onCloseClicked();
            }
        });

        addButtonToFooter(btnClose);

        diffViewer.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Display diff for current file in console
     *
     * @param content
     */
    @Override
    public void showDiff(String content) {
        final String colorizedContent = colorizeDiff(content);
        diffViewer.setHTML(colorizedContent);
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

    private String colorizeDiff(String origin) {
        StringBuilder html = new StringBuilder();
        html.append("<pre>");

        for (String line : Splitter.on("\n").splitToList(origin)) {
            final String prefix = line.substring(0, 1);
            final String sanitizedLine = new SafeHtmlBuilder().appendEscaped(line).toSafeHtml().asString();
            html.append("<span style=\"color:")
                .append(lineRules.containsKey(prefix) ? lineRules.get(prefix) : lineRules.get("default"))
                .append(";\">")
                .append(sanitizedLine)
                .append("</span>")
                .append("\n");

        }
        html.append("</pre>");
        return html.toString();
    }
}
