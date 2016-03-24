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
package org.eclipse.che.ide.part.explorer.project;

import org.eclipse.che.ide.ui.window.Window;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/** @author Artem Zatsarynnyi */
public class ProjectProblemDialog extends Window {

    private static ProjectProblemDialogUiBinder uiBinder = GWT.create(ProjectProblemDialogUiBinder.class);
    private final AskHandler handler;
    @UiField
    SimplePanel message;

    Button configureButton;
    Button keepBlankButton;

    /**
     * Creates new dialog.
     *
     * @param title
     *         the title for popup window
     * @param question
     *         the question that user must interact
     * @param handler
     *         the handler that call after user interact
     */
    public ProjectProblemDialog(String title, String question, final AskHandler handler) {
        this.handler = handler;
        setTitle(title);
        Widget widget = uiBinder.createAndBindUi(this);
        setWidget(widget);
        message.addStyleName(resources.windowCss().label());
        message.getElement().setInnerHTML(question);

        configureButton = createButton("Configure...", "problem-dialog-configure", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onConfigure();
                onClose();
            }
        });
        keepBlankButton = createPrimaryButton("Keep Blank", "problem-dialog-keepBlank", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onKeepBlank();
                onClose();
            }
        });
        addButtonToFooter(configureButton);
        addButtonToFooter(keepBlankButton);
    }

    /** {@inheritDoc} */
    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(configureButton)) {
            handler.onConfigure();
            onClose();
            return;
        }

        if (isWidgetFocused(keepBlankButton)) {
            handler.onKeepBlank();
            onClose();
        }
    }

    @Override
    protected void onClose() {
        hide();
    }

    interface ProjectProblemDialogUiBinder extends UiBinder<Widget, ProjectProblemDialog> {
    }

    public abstract static class AskHandler {
        /** Call if user click 'Configure' button. */
        public abstract void onConfigure();

        /** Call if user click 'Keep Blank' button. */
        public abstract void onKeepBlank();
    }
}
