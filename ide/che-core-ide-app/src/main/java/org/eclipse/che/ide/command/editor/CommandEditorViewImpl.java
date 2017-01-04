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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.radiobuttongroup.RadioButtonGroup;
import org.eclipse.che.ide.ui.window.Window;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Implementation of {@link CommandEditorView}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditorViewImpl extends Composite implements CommandEditorView {

    private static final CommandEditorViewImplUiBinder UI_BINDER        = GWT.create(CommandEditorViewImplUiBinder.class);
    private static final Window.Resources              WINDOW_RESOURCES = GWT.create(Window.Resources.class);

    @UiField
    RadioButtonGroup pagesSwitcher;

    @UiField
    Button saveButton;

    @UiField
    Button testButton;

    @UiField
    DeckPanel pagesPanel;

    /** The total count of added pages. */
    private int pagesCount;

    /** The delegate to receive events from this view. */
    private ActionDelegate delegate;

    @Inject
    public CommandEditorViewImpl(CommandResources resources) {
        initWidget(UI_BINDER.createAndBindUi(this));

        setSaveEnabled(false);

        saveButton.addStyleName(WINDOW_RESOURCES.windowCss().primaryButton());
        testButton.getElement().appendChild(new SVGImage(resources.execute()).getElement());
    }

    @Override
    public void addPage(IsWidget page, String title, String tooltip) {
        final int pageIndex = pagesCount;

        pagesSwitcher.addButton(title, tooltip, null, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pagesPanel.showWidget(pageIndex);
            }
        });

        pagesPanel.add(page);

        if (pagesCount == 0) {
            pagesSwitcher.selectButton(0);
            pagesPanel.showWidget(0);
        }

        pagesCount++;
    }

    @Override
    public void setSaveEnabled(boolean enable) {
        saveButton.setEnabled(enable);
    }

    @UiHandler("saveButton")
    public void handleSaveButton(ClickEvent clickEvent) {
        delegate.onCommandSave();
    }

    @UiHandler("testButton")
    public void handleTestButton(ClickEvent clickEvent) {
        delegate.onCommandTest();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface CommandEditorViewImplUiBinder extends UiBinder<Widget, CommandEditorViewImpl> {
    }
}
