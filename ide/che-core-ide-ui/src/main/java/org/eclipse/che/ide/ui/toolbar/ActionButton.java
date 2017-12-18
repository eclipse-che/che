/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.toolbar;

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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.PropertyChangeEvent;
import org.eclipse.che.ide.api.action.PropertyChangeListener;
import org.eclipse.che.ide.ui.ElementWidget;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;

/**
 * Toolbar image button.
 *
 * @author Evgen Vidolob
 */
public class ActionButton extends Composite
    implements MouseOverHandler, MouseOutHandler, MouseDownHandler, MouseUpHandler, ClickHandler {

  private final Presentation presentation;

  /** Command which will be executed when button was pressed. */
  protected Action action;

  private FlowPanel panel;

  /** Is enabled. */
  private boolean enabled = true;

  /** Is button selected. */
  private boolean selected = false;

  private ActionManager actionManager;
  private ActionButtonSynchronizer actionButtonSynchronizer;
  private ToolbarResources toolbarResources;

  /** Tooltip */
  private Tooltip tooltip;

  public ActionButton(
      Action action,
      ActionManager actionManager,
      Presentation presentation,
      ToolbarResources toolbarResources) {
    this.actionManager = actionManager;
    this.toolbarResources = toolbarResources;
    panel = new FlowPanel();

    initWidget(panel);
    panel.setStyleName(toolbarResources.toolbar().iconButtonPanel());
    this.action = action;
    this.presentation = presentation;
    addDomHandlers();
    renderImage();
    setEnabled(presentation.isEnabled());
    setVisible(presentation.isVisible());

    if (presentation.getDescription() != null) {
      tooltip =
          Tooltip.create(
              (elemental.dom.Element) panel.getElement(),
              PositionController.VerticalAlign.BOTTOM,
              PositionController.HorizontalAlign.MIDDLE,
              presentation.getDescription());
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void onLoad() {
    super.onLoad();
    if (actionButtonSynchronizer == null) {
      actionButtonSynchronizer = new ActionButtonSynchronizer();
      presentation.addPropertyChangeListener(actionButtonSynchronizer);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void onUnload() {
    super.onUnload();
    if (actionButtonSynchronizer != null) {
      presentation.removePropertyChangeListener(actionButtonSynchronizer);
      actionButtonSynchronizer = null;
    }
  }

  private void addDomHandlers() {
    panel.addDomHandler(this, MouseOverEvent.getType());
    panel.addDomHandler(this, MouseOutEvent.getType());
    panel.addDomHandler(this, MouseDownEvent.getType());
    panel.addDomHandler(this, MouseUpEvent.getType());
    panel.addDomHandler(this, ClickEvent.getType());
  }

  /** Redraw icon. */
  private void renderImage() {
    panel.clear();

    if (presentation.getImageElement() != null) {
      ElementWidget image = new ElementWidget(presentation.getImageElement());
      image.getElement().setAttribute("class", toolbarResources.toolbar().iconButtonIcon());
      panel.add(image);

    } else if (presentation.getHTMLResource() != null) {
      FlowPanel icon = new FlowPanel();
      icon.setStyleName(toolbarResources.toolbar().iconButtonIcon());

      FlowPanel inner = new FlowPanel();
      inner.setStyleName(toolbarResources.toolbar().iconButtonIconInner());
      inner.getElement().setInnerHTML(presentation.getHTMLResource());
      icon.add(inner);

      panel.add(inner);
    }
  }

  /** {@inheritDoc} */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if (enabled) {
      removeStyleName(toolbarResources.toolbar().disabled());
    } else {
      if (selected) {
        panel.setStyleName(toolbarResources.toolbar().iconButtonPanelSelected());
      } else {
        panel.setStyleName(toolbarResources.toolbar().iconButtonPanel());
      }

      addStyleName(toolbarResources.toolbar().disabled());
    }
    this.ensureDebugId("ActionButton/" + actionManager.getId(action) + "-" + enabled);
  }

  /** {@inheritDoc} */
  public void setSelected(boolean selected) {
    this.selected = selected;
    if (selected) {
      panel.setStyleName(toolbarResources.toolbar().iconButtonPanelSelected());
    }
  }

  /** Mouse Over handler. */
  @Override
  public void onMouseOver(MouseOverEvent event) {}

  /** Mouse Out handler. */
  @Override
  public void onMouseOut(MouseOutEvent event) {
    if (!enabled) {
      return;
    }

    if (selected) {
      panel.setStyleName(toolbarResources.toolbar().iconButtonPanelSelected());
    } else {
      panel.setStyleName(toolbarResources.toolbar().iconButtonPanel());
    }
  }

  /** Mouse Down handler. */
  @Override
  public void onMouseDown(MouseDownEvent event) {
    if (!enabled) {
      return;
    }

    if (selected) {
      panel.setStyleName(toolbarResources.toolbar().iconButtonPanelSelectedDown());
    } else {
      panel.setStyleName(toolbarResources.toolbar().iconButtonPanelDown());
    }
  }

  /** Mouse Up handler. */
  @Override
  public void onMouseUp(MouseUpEvent event) {}

  /** Mouse Click handler. */
  @Override
  public void onClick(ClickEvent event) {
    if (!enabled) {
      return;
    }

    // todo handle popup group
    ActionEvent e = new ActionEvent(presentation, actionManager);
    if (action instanceof ActionGroup
        && !(action instanceof CustomComponentAction)
        && ((ActionGroup) action).isPopup()) {

    } else {
      action.actionPerformed(e);
    }
  }

  private class ActionButtonSynchronizer implements PropertyChangeListener {
    protected static final String SELECTED_PROPERTY_NAME = "selected";

    @Override
    public void onPropertyChange(PropertyChangeEvent e) {
      String propertyName = e.getPropertyName();
      if (Presentation.PROP_TEXT.equals(propertyName)) {
        if (tooltip != null && e.getNewValue() instanceof String) {
          tooltip.setTitle((String) e.getNewValue());
        }
      } else if (Presentation.PROP_ENABLED.equals(propertyName)) {
        setEnabled((Boolean) e.getNewValue());
      } else if (Presentation.PROP_ICON.equals(propertyName)) {
        renderImage();
      } else if (Presentation.PROP_VISIBLE.equals(propertyName)) {
        setVisible((Boolean) e.getNewValue());
      } else if (SELECTED_PROPERTY_NAME.equals(propertyName)) {
        setSelected((Boolean) e.getNewValue());
      }
    }
  }
}
