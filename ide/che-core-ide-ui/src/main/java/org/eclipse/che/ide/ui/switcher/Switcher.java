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
package org.eclipse.che.ide.ui.switcher;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimpleCheckBox;

/**
 * UI element with two states boolean states: "ON" - true, "OFF" - false.
 * User switches the state by click.
 *
 * @author Ann Shumilova
 */
public class Switcher extends Composite implements HasValue<Boolean> {
    private static final Resources resources = GWT.create(Resources.class);

    static {
        resources.switcherCSS().ensureInjected();
    }

    SimpleCheckBox checkbox;

    public interface Resources extends ClientBundle {
        public interface SwitcherCSS extends CssResource {
            String onoffswitchInner();

            String onoffswitch();

            String onoffswitchSwitch();

            String onoffswitchLabel();

            String onoffswitchCheckbox();

        }

        @Source({"switcher.css", "org/eclipse/che/ide/api/ui/style.css"})
        SwitcherCSS switcherCSS();
    }


    public Switcher() {
        FlowPanel mainPanel = new FlowPanel();
        mainPanel.setStyleName(resources.switcherCSS().onoffswitch());

        checkbox = new SimpleCheckBox();
        checkbox.getElement().setId("switcher");
        checkbox.setName("onoffswitch");
        checkbox.setStyleName(resources.switcherCSS().onoffswitchCheckbox());
        mainPanel.add(checkbox);

        Element label = DOM.createLabel();
        label.setClassName(resources.switcherCSS().onoffswitchLabel());
        label.setAttribute("for", "switcher");

        Element inner = DOM.createDiv();
        inner.setClassName(resources.switcherCSS().onoffswitchInner());
        label.appendChild(inner);

        Element sw = DOM.createDiv();
        sw.setClassName(resources.switcherCSS().onoffswitchSwitch());
        label.appendChild(sw);

        mainPanel.getElement().appendChild(label);

        initWidget(mainPanel);
    }

    /** {@inheritDoc} */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
        return checkbox.addValueChangeHandler(handler);
    }


    /** {@inheritDoc} */
    @Override
    public Boolean getValue() {
        return checkbox.getValue();
    }


    /** {@inheritDoc} */
    @Override
    public void setValue(Boolean value) {
        checkbox.setValue(value);
    }


    /** {@inheritDoc} */
    @Override
    public void setValue(Boolean value, boolean fireEvents) {
        checkbox.setValue(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

}
