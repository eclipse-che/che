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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.ui.window.Window;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Implementation of {@link CommandEditorView}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandEditorViewImpl extends Composite implements CommandEditorView {

    private static final CommandEditorViewImplUiBinder UI_BINDER        = GWT.create(CommandEditorViewImplUiBinder.class);
    private static final Window.Resources              WINDOW_RESOURCES = GWT.create(Window.Resources.class);

    @UiField
    CommandResources resources;

    @UiField
    Button testButton;

    @UiField
    Button cancelButton;

    @UiField
    Button saveButton;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    FlowPanel pagesPanel;

    /** The delegate to receive events from this view. */
    private ActionDelegate delegate;

    @Inject
    public CommandEditorViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));

        setSaveEnabled(false);

        saveButton.addStyleName(WINDOW_RESOURCES.windowCss().primaryButton());
    }

    @Override
    public void addPage(IsWidget page, String title) {
        if (pagesPanel.getWidgetCount() == 0) {
            pagesPanel.add(page);
            return;
        }

        final DisclosurePanel disclosurePanel = new DisclosurePanel(resources.iconExpanded(), resources.iconCollapsed(), title);
        disclosurePanel.setAnimationEnabled(true);
        disclosurePanel.setContent(page.asWidget());
        disclosurePanel.setOpen(true);

        disclosurePanel.getElement().getStyle().setMarginTop(8, PX);
        disclosurePanel.getElement().getStyle().setMarginBottom(8, PX);
        disclosurePanel.getHeader().getElement().getStyle().setMarginBottom(8, PX);

        pagesPanel.add(disclosurePanel);
    }

    @Override
    public void setSaveEnabled(boolean enable) {
        saveButton.setEnabled(enable);
    }

    @UiHandler("testButton")
    public void handleTestButton(ClickEvent clickEvent) {
        delegate.onCommandTest();
    }

    @UiHandler("cancelButton")
    public void handleCancelButton(ClickEvent clickEvent) {
        delegate.onCommandCancel();
    }

    @UiHandler("saveButton")
    public void handleSaveButton(ClickEvent clickEvent) {
        delegate.onCommandSave();
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    interface CommandEditorViewImplUiBinder extends UiBinder<Widget, CommandEditorViewImpl> {
    }
}
