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
package org.eclipse.che.ide.ui.radiobuttongroup;

import static com.google.gwt.dom.client.Element.as;
import static com.google.gwt.dom.client.Style.Unit.PX;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.UUID;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Represents mutually-exclusion set of buttons. Turning on one of those buttons turns off all other
 * buttons in the group. Initially, all buttons in the group are unselected.
 *
 * @author Artem Zatsarynnyi
 */
public class RadioButtonGroup extends Composite {

  private static final Resources resources = GWT.create(Resources.class);

  private final String GROUP_NAME;

  private List<RadioButton> buttons;
  private FlowPanel mainPanel;

  /** Creates new mutually-exclusion group of buttons. */
  public RadioButtonGroup() {
    GROUP_NAME = "buttons-group-" + UUID.uuid();

    buttons = new ArrayList<>();
    mainPanel = new FlowPanel();
    mainPanel.setStyleName(resources.getCSS().mainPanel());

    initWidget(mainPanel);
  }

  /**
   * Adds the new radio button to the group.
   *
   * @param label radio button's label
   * @param title radio button's tooltip
   * @param icon radio button's icon
   * @param clickHandler click handler
   */
  public void addButton(
      String label, String title, @Nullable SVGResource icon, ClickHandler clickHandler) {
    final RadioButton radioButton = new RadioButton(GROUP_NAME, label);
    radioButton.setTitle(title);
    radioButton.setStyleName(resources.getCSS().button());
    radioButton.addClickHandler(clickHandler);

    final Element radioButtonElement = radioButton.getElement();
    final Node labelNode = radioButtonElement.getLastChild();

    if (icon != null) {
      labelNode.insertFirst(new SVGImage(icon).getElement());
    } else {
      radioButtonElement.getStyle().setWidth(90, PX);
      as(labelNode).getStyle().setWidth(90, PX);
    }

    mainPanel.add(radioButton);
    buttons.add(radioButton);
  }

  /** Select the button with the specified index. */
  public void selectButton(int index) {
    if (buttons.size() > index) {
      buttons.get(index).setValue(true);
    }
  }

  /** The resource interface for the {@link RadioButtonGroup} widget. */
  public interface Resources extends ClientBundle {

    /** Returns the CSS resource for the {@link RadioButtonGroup} widget. */
    @Source({"radio-button-group.css", "org/eclipse/che/ide/api/ui/style.css"})
    CSS getCSS();

    /** The CssResource interface for the {@link RadioButtonGroup} widget. */
    interface CSS extends CssResource {

      /** Returns the CSS class name for main panel (group itself). */
      String mainPanel();

      /** Returns the CSS class name for button in group. */
      @ClassName("mx-button")
      String button();
    }
  }

  static {
    resources.getCSS().ensureInjected();
  }
}
