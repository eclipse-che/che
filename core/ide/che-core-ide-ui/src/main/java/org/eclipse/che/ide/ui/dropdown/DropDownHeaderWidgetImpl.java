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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.commons.annotation.Nullable;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Class provides general view representation for header of drop down list.
 *
 * @author Valeriy Svydenko
 */
public class DropDownHeaderWidgetImpl extends Composite implements ClickHandler,
                                                                   MouseDownHandler,
                                                                   MouseUpHandler,
                                                                   MouseOutHandler,
                                                                   MouseOverHandler,
                                                                   DropDownHeaderWidget {
    private static final Resources resources = GWT.create(Resources.class);

    static {
        resources.dropdownListCss().ensureInjected();
    }

    interface HeaderWidgetImplUiBinder extends UiBinder<Widget, DropDownHeaderWidgetImpl> {
    }

    private static final HeaderWidgetImplUiBinder UI_BINDER = GWT.create(HeaderWidgetImplUiBinder.class);

    @UiField
    FlowPanel marker;
    @UiField
    Label     selectedElementName;
    @UiField
    FlowPanel selectedElement;

    @UiField
    FlowPanel listHeader;

    private final DropDownListMenu dropDownListMenu;
    private final String           listId;
    private       String           selectedName;
    private       ActionDelegate   delegate;

    @AssistedInject
    public DropDownHeaderWidgetImpl(DropDownListMenu dropDownListMenu, @NotNull @Assisted String listId) {
        this.dropDownListMenu = dropDownListMenu;
        this.listId = listId;

        initWidget(UI_BINDER.createAndBindUi(this));

        listHeader.setStyleName(resources.dropdownListCss().onMouseOut());

        marker.getElement().appendChild(resources.expansionImage().getSvg().getElement());
        marker.addStyleName(resources.dropdownListCss().expandedImage());

        addDomHandler(this, ClickEvent.getType());
        addDomHandler(this, MouseDownEvent.getType());
        addDomHandler(this, MouseUpEvent.getType());
        addDomHandler(this, MouseOutEvent.getType());
        addDomHandler(this, MouseOverEvent.getType());
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void selectElement(@NotNull String title) {
        selectedName = title;
        selectedElementName.setText(title);
        delegate.onSelect();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getSelectedElementName() {
        return selectedName;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(ClickEvent event) {
        int left = getAbsoluteLeft() + listHeader.getOffsetWidth();
        int top = getAbsoluteTop() + listHeader.getOffsetHeight();
        dropDownListMenu.show(left, top, listId);
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseDown(MouseDownEvent event) {
        listHeader.setStyleName(resources.dropdownListCss().onClick());
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseUp(MouseUpEvent event) {
        listHeader.setStyleName(resources.dropdownListCss().onMouseOut());
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseOut(MouseOutEvent event) {
        listHeader.setStyleName(resources.dropdownListCss().onMouseOut());
    }

    /** {@inheritDoc} */
    @Override
    public void onMouseOver(MouseOverEvent event) {
        listHeader.setStyleName(resources.dropdownListCss().onMouseOver());
    }

    /** Item style selectors for a categories list item. */
    public interface DropdownCss extends CssResource {
        String expandedImage();

        String onMouseOver();

        String onMouseOut();

        String onClick();

        String dropDownListMenu();
    }

    public interface Resources extends ClientBundle {
        @Source({"DropdownList.css", "org/eclipse/che/ide/api/ui/style.css"})
        DropdownCss dropdownListCss();

        @Source("expansionIcon.svg")
        SVGResource expansionImage();
    }
}