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
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
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
@Singleton
public class SubversionOutputConsoleViewImpl extends BaseView<SubversionOutputConsoleView.ActionDelegate> implements
                                                                                                                 SubversionOutputConsoleView {
    @UiTemplate(value = "SubversionOutputConsoleViewImpl.ui.xml")
    interface SubversionOutputConsoleViewImplUiBinder extends UiBinder<Widget, SubversionOutputConsoleViewImpl> { }

    private static SubversionOutputConsoleViewImplUiBinder uiBinder = GWT.create(SubversionOutputConsoleViewImplUiBinder.class);

    @UiField
    FlowPanel buttons;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    FlowPanel consoleArea;

    @Inject
    public SubversionOutputConsoleViewImpl(final ConsoleButtonFactory consoleButtonFactory, final PartStackUIResources resources,
                                           final Resources coreResources, final SubversionExtensionLocalizationConstants constants) {
        super(resources);

        setContentWidget(uiBinder.createAndBindUi(this));

        minimizeButton.ensureDebugId("console-minimizeBut");

        ConsoleButton clearButton = consoleButtonFactory.createConsoleButton(constants.consoleClearButton(), coreResources.clear());
        clearButton.setDelegate(new ConsoleButton.ActionDelegate() {
            @Override
            public void onButtonClicked() {delegate.onClearClicked();}
        });

        buttons.add(clearButton);
    }

    @Override
    public void print(String text) {
        HTML html = new HTML();
        html.setHTML("<pre style='margin:0px; font-size: 11px;'>" + text + "</pre>");
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
