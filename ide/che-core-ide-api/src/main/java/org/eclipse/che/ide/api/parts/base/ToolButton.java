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
package org.eclipse.che.ide.api.parts.base;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Button which can be added to the tool bar.
 *
 * @author Codenvy crowd
 */
public class ToolButton extends Composite implements HasClickHandlers {

    /** UIBinder class for this TabButton. */
    interface TabButtonUiBinder extends UiBinder<Widget, ToolButton> {
    }

    /** UIBinder for this TabButton. */
    private static TabButtonUiBinder uiBinder = GWT.create(TabButtonUiBinder.class);

    @UiField
    FlowPanel iconPanel;

    /**
     * Creates new button based on SVG image.
     *
     * @param image
     *         SVG image
     */
    public ToolButton(SVGImage image) {
        initWidget(uiBinder.createAndBindUi(this));
        iconPanel.add(image);

        blockDoubleClicking();
    }

    /**
     * Creates new button containing new Image created from HTML.
     *
     * @param htmlImageResource
     *         HTML content for new image
     */
    public ToolButton(String htmlImageResource) {
        initWidget(uiBinder.createAndBindUi(this));

        FlowPanel image = new FlowPanel();
        image.getElement().setInnerHTML(htmlImageResource);
        iconPanel.add(image);

        blockDoubleClicking();
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler clickHandler) {
        return iconPanel.addDomHandler(clickHandler, ClickEvent.getType());
    }

    /**
     * Blocks double clicking on the button and on the parent element.
     */
    private void blockDoubleClicking() {
        addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                event.stopPropagation();
                event.preventDefault();
            }
        }, DoubleClickEvent.getType());
    }

}
