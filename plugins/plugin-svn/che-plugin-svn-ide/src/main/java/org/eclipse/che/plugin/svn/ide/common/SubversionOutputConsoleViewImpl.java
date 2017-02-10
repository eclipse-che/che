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
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.api.parts.PartStackUIResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.ui.button.ConsoleButton;
import org.eclipse.che.ide.ui.button.ConsoleButtonFactory;

/**
 * Implementation of {@link SubversionOutputConsoleView}.
 */
public class SubversionOutputConsoleViewImpl extends Composite implements SubversionOutputConsoleView {

    private ActionDelegate delegate;

    interface SubversionOutputConsoleViewImplUiBinder extends UiBinder<Widget, SubversionOutputConsoleViewImpl> { }

    @UiField
    FlowPanel buttons;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    FlowPanel consoleArea;

    @Inject
    public SubversionOutputConsoleViewImpl(final SubversionOutputConsoleViewImplUiBinder uiBinder,
                                           final ConsoleButtonFactory consoleButtonFactory,
                                           final PartStackUIResources resources,
                                           final SubversionExtensionLocalizationConstants constants) {

        initWidget(uiBinder.createAndBindUi(this));

        ConsoleButton clearButton = consoleButtonFactory.createConsoleButton(constants.consoleClearButton(), resources.erase());
        clearButton.setDelegate(new ConsoleButton.ActionDelegate() {
            @Override
            public void onButtonClicked() {delegate.onClearClicked();}
        });

        buttons.add(clearButton);

        ConsoleButton scrollButton = consoleButtonFactory.createConsoleButton(constants.consoleScrollButton(), resources.arrowBottom());
        scrollButton.setDelegate(new ConsoleButton.ActionDelegate() {
            @Override
            public void onButtonClicked() {delegate.onScrollClicked();}
        });

        buttons.add(scrollButton);
    }

    /**
     * Sets the delegate to receive events from this view.
     *
     * @param delegate
     */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void print(String text) {
        String preStyle = " style='margin:0px; font-size: 12px;' ";

        HTML html = new HTML();
        html.setHTML("<pre" + preStyle + ">" + SimpleHtmlSanitizer.sanitizeHtml(text).asString() + "</pre>");
        html.getElement().setAttribute("style", "padding-left: 2px;");

        consoleArea.add(html);
    }

    @Override
    public void print(String text, String color) {
        String preStyle = " style='margin:0px; font-size: 12px;' ";

        HTML html = new HTML();
        html.setHTML("<pre" + preStyle + "><span style='color:" + SimpleHtmlSanitizer.sanitizeHtml(color).asString() +
                     ";'>" + SimpleHtmlSanitizer.sanitizeHtml(text).asString() + "</span></pre>");

        html.getElement().setAttribute("style", "padding-left: 2px;");
        consoleArea.add(html);
    }

    /**
     * Print text with a given style in view.
     *
     * @param text
     *         The text to display
     * @param style
     */
    @Override
    public void printPredefinedStyle(String text, String style) {
        String preStyle = " style='margin:0px; font-size: 12px;' ";

        HTML html = new HTML();

        html.setHTML("<pre" + preStyle + "><span style='" + SimpleHtmlSanitizer.sanitizeHtml(style).asString() + "'>" +
                     SimpleHtmlSanitizer.sanitizeHtml(text).asString() + "</span></pre>");

        html.getElement().setAttribute("style", "padding-left: 2px;");
        consoleArea.add(html);
    }

    @Override
    public void clear() {
        consoleArea.clear();
    }

    @Override
    public void scrollBottom() {
        scrollPanel.getElement().setScrollTop(scrollPanel.getElement().getScrollHeight());
    }

}
