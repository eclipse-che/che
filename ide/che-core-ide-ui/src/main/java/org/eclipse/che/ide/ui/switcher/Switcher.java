/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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
 * UI element with two states boolean states: "ON" - true, "OFF" - false. User switches the state by
 * click.
 *
 * @author Ann Shumilova
 */
public class Switcher extends Composite implements HasValue<Boolean> {

  private static final Resources RESOURCES = GWT.create(Resources.class);

  SimpleCheckBox checkbox;

  public Switcher() {
    FlowPanel mainPanel = new FlowPanel();
    mainPanel.setStyleName(RESOURCES.switcherCSS().onoffswitch());

    final String elementId = DOM.createUniqueId();

    checkbox = new SimpleCheckBox();
    checkbox.getElement().setId(elementId);
    checkbox.setName("onoffswitch");
    checkbox.setStyleName(RESOURCES.switcherCSS().onoffswitchCheckbox());
    mainPanel.add(checkbox);

    Element label = DOM.createLabel();
    label.setClassName(RESOURCES.switcherCSS().onoffswitchLabel());
    label.setAttribute("for", elementId);

    Element inner = DOM.createDiv();
    inner.setClassName(RESOURCES.switcherCSS().onoffswitchInner());
    label.appendChild(inner);

    Element sw = DOM.createDiv();
    sw.setClassName(RESOURCES.switcherCSS().onoffswitchSwitch());
    label.appendChild(sw);

    mainPanel.getElement().appendChild(label);

    initWidget(mainPanel);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    return checkbox.addValueChangeHandler(handler);
  }

  @Override
  public Boolean getValue() {
    return checkbox.getValue();
  }

  @Override
  public void setValue(Boolean value) {
    checkbox.setValue(value);
  }

  @Override
  public void setValue(Boolean value, boolean fireEvents) {
    checkbox.setValue(value);

    if (fireEvents) {
      ValueChangeEvent.fire(this, value);
    }
  }

  public interface Resources extends ClientBundle {
    @Source({"switcher.css", "org/eclipse/che/ide/api/ui/style.css"})
    SwitcherCSS switcherCSS();

    interface SwitcherCSS extends CssResource {
      String onoffswitchInner();

      String onoffswitch();

      String onoffswitchSwitch();

      String onoffswitchLabel();

      String onoffswitchCheckbox();
    }
  }

  static {
    RESOURCES.switcherCSS().ensureInjected();
  }
}
