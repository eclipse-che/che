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
package org.eclipse.che.ide.extension.machine.client.machine.console;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

/**
 * Implementation of {@link MachineConsoleView}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachineConsoleViewImpl extends BaseView<MachineConsoleView.ActionDelegate> implements MachineConsoleView {

    private static final String PRE_STYLE = "style='margin:0px;'";

    @UiField
    SimplePanel toolbarPanel;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    FlowPanel   consoleLines;

    @Inject
    public MachineConsoleViewImpl(PartStackUIResources resources, MachineConsoleViewImplUiBinder uiBinder) {
        super(resources);

        setContentWidget(uiBinder.createAndBindUi(this));

        minimizeButton.ensureDebugId("machine-console-minimizeButton");

        // this hack used for adding box shadow effect to toolbar
        toolbarPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
        toolbarPanel.getElement().getParentElement().getStyle().setZIndex(1);

        scrollPanel.getElement().setTabIndex(0);
    }

    /** {@inheritDoc} */
    @Override
    public AcceptsOneWidget getToolbarPanel() {
        return toolbarPanel;
    }

    /** {@inheritDoc} */
    @Override
    public void print(String message) {
        final HTML html = new HTML(buildSafeHtmlMessage(message));
        html.getElement().getStyle().setPaddingLeft(2, Style.Unit.PX);
        consoleLines.add(html);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        consoleLines.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void scrollBottom() {
        scrollPanel.getElement().setScrollTop(scrollPanel.getElement().getScrollHeight());
    }

    /** Return sanitized message (with all restricted HTML-tags escaped) in {@link SafeHtml}. */
    private SafeHtml buildSafeHtmlMessage(String message) {
        return new SafeHtmlBuilder()
                .appendHtmlConstant("<pre " + PRE_STYLE + ">")
                .append(SimpleHtmlSanitizer.sanitizeHtml(message))
                .appendHtmlConstant("</pre>")
                .toSafeHtml();
    }

    @Override
    protected void focusView() {
        scrollPanel.getElement().focus();
    }

    interface MachineConsoleViewImplUiBinder extends UiBinder<Widget, MachineConsoleViewImpl> {
    }
}
