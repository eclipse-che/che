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
package org.eclipse.che.ide.ui.button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.ui.tooltip.TooltipWidget;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
public class ConsoleButtonImpl extends Composite implements ConsoleButton, ClickHandler, MouseOverHandler, MouseOutHandler {

    interface ConsoleButtonImplUiBinder extends UiBinder<Widget, ConsoleButtonImpl> {
    }

    public static final  int                       TOP_TOOLTIP_SHIFT = 35;
    private static final ConsoleButtonImplUiBinder UI_BINDER         = GWT.create(ConsoleButtonImplUiBinder.class);

    @UiField
    SimpleLayoutPanel image;

    private final ButtonResources resources;
    private final TooltipWidget tooltip;
    private final SVGImage icon;

    private ActionDelegate delegate;

    @Inject
    public ConsoleButtonImpl(ButtonResources resources,
                             TooltipWidget tooltip,
                             @NotNull @Assisted String prompt,
                             @NotNull @Assisted SVGResource image) {
        this.resources = resources;
        this.tooltip = tooltip;
        this.tooltip.setDescription(prompt);

        resources.buttonCss().ensureInjected();

        initWidget(UI_BINDER.createAndBindUi(this));

        icon = new SVGImage(image);
        icon.getElement().setAttribute("class", resources.buttonCss().mainButtonIcon());

        setCheckedStatus(false);

        addDomHandler(this, ClickEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());
        addDomHandler(this, MouseOverEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public void setCheckedStatus(boolean isChecked) {
        ButtonResources.Css buttonCss = resources.buttonCss();

        if (isChecked) {
            icon.removeClassNameBaseVal(buttonCss.whiteColor());
            icon.addClassNameBaseVal(buttonCss.activeConsoleButton());
        } else {
            icon.removeClassNameBaseVal(buttonCss.activeConsoleButton());
            icon.addClassNameBaseVal(buttonCss.whiteColor());
        }

        image.getElement().appendChild(icon.getSvgElement().getElement());
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent clickEvent) {
        delegate.onButtonClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseOut(MouseOutEvent mouseOutEvent) {
        tooltip.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseOver(MouseOverEvent mouseOverEvent) {
        tooltip.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + TOP_TOOLTIP_SHIFT);

        tooltip.show();
    }
}