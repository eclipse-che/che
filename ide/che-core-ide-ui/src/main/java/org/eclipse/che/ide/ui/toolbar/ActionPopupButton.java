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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.inject.Provider;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.ActionSelectedHandler;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.PropertyChangeEvent;
import org.eclipse.che.ide.api.action.PropertyChangeListener;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.ElementWidget;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class ActionPopupButton extends Composite
    implements CloseMenuHandler, ActionSelectedHandler {

  private final ActionGroup action;
  private final ActionManager actionManager;
  private final Provider<PerspectiveManager> managerProvider;
  private final Presentation presentation;

  private ActionButtonSynchronizer actionButtonSynchronizer;
  private KeyBindingAgent keyBindingAgent;
  private PresentationFactory presentationFactory;
  private ToolbarResources toolbarResources;

  /** Enabled state. True as default. */
  private boolean enabled = true;

  /** Lock Layer uses for locking rest of the screen, which does not covered by Popup Menu. */
  private MenuLockLayer lockLayer;

  /** Popup Menu button panel (<div> HTML element). */
  private ButtonPanel panel;

  /** Has instance if Popup Menu is opened. */
  private PopupMenu popupMenu;

  /** Tooltip */
  private Tooltip tooltip;

  /** Create Popup Menu Button with specified icons for enabled and disabled states. */
  public ActionPopupButton(
      final ActionGroup action,
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      final PresentationFactory presentationFactory,
      Provider<PerspectiveManager> managerProvider,
      ToolbarResources toolbarResources) {
    this.action = action;
    this.actionManager = actionManager;
    this.keyBindingAgent = keyBindingAgent;
    this.presentationFactory = presentationFactory;
    this.managerProvider = managerProvider;
    this.toolbarResources = toolbarResources;

    presentation = presentationFactory.getPresentation(action);

    panel = new ButtonPanel();
    initWidget(panel);

    panel.setStyleName(toolbarResources.toolbar().popupButtonPanel());

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

    this.ensureDebugId("PopupButton/" + action.getTemplatePresentation().getText());
  }

  /** Redraw the icon. */
  private void renderImage() {
    panel.clear();

    if (presentation.getImageElement() != null) {
      ElementWidget image = new ElementWidget(presentation.getImageElement());
      image.getElement().setAttribute("class", toolbarResources.toolbar().popupButtonIcon());
      panel.add(image);

    } else if (presentation.getHTMLResource() != null) {
      FlowPanel icon = new FlowPanel();
      icon.setStyleName(toolbarResources.toolbar().iconButtonIcon());

      FlowPanel inner = new FlowPanel();
      inner.setStyleName(toolbarResources.toolbar().popupButtonIconInner());
      inner.getElement().setInnerHTML(presentation.getHTMLResource());
      icon.add(inner);

      panel.add(inner);
    }

    InlineLabel caret = new InlineLabel("");
    caret.setStyleName(toolbarResources.toolbar().caret());
    panel.add(caret);
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

  /** Closes Popup Menu ( if opened ) and sets style of this Popup Menu Button to default. */
  protected void closePopupMenu() {
    if (popupMenu != null) {
      popupMenu.removeFromParent();
      popupMenu = null;
    }

    if (lockLayer != null) {
      lockLayer.removeFromParent();
      lockLayer = null;
    }

    panel.setStyleName(toolbarResources.toolbar().popupButtonPanel());
  }

  /**
   * Get is this button enabled.
   *
   * @return is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Set is enabled.
   *
   * @param enabled is enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    if (enabled) {
      panel.getElement().removeClassName(toolbarResources.toolbar().disabled());
    } else {
      panel.getElement().addClassName(toolbarResources.toolbar().disabled());
    }
  }

  /** {@inheritDoc} */
  public void onCloseMenu() {
    closePopupMenu();
  }

  /** Mouse Down handler. */
  private void onMouseDown() {
    panel.setStyleName(toolbarResources.toolbar().popupButtonPanelDown());
  }

  /** Mouse Out Handler. */
  private void onMouseOut() {
    if (popupMenu != null) {
      return;
    }

    panel.setStyleName(toolbarResources.toolbar().popupButtonPanel());
  }

  private void onMouseClick() {
    openPopupMenu();
  }

  /** Mouse Over handler. */
  private void onMouseOver() {}

  /** Opens Popup Menu. */
  public void openPopupMenu() {
    lockLayer = new MenuLockLayer(this);

    popupMenu =
        new PopupMenu(
            action,
            actionManager,
            managerProvider,
            presentationFactory,
            lockLayer,
            this,
            keyBindingAgent,
            "toolbar");
    lockLayer.add(popupMenu);

    int left = getAbsoluteLeft();
    int top = getAbsoluteTop() + 24;
    popupMenu.getElement().getStyle().setTop(top, com.google.gwt.dom.client.Style.Unit.PX);
    popupMenu.getElement().getStyle().setLeft(left, com.google.gwt.dom.client.Style.Unit.PX);
  }

  /** {@inheritDoc} */
  @Override
  public void onActionSelected(Action action) {
    closePopupMenu();
  }

  /** This class uses to handling mouse events on Popup Button. */
  private class ButtonPanel extends FlowPanel {

    public ButtonPanel() {
      sinkEvents(
          Event.ONMOUSEOVER
              | Event.ONMOUSEOUT
              | Event.ONMOUSEDOWN
              | Event.ONMOUSEUP
              | Event.ONCLICK);
    }

    /** Handle browser's events. */
    @Override
    public void onBrowserEvent(Event event) {
      if (!enabled) {
        return;
      }

      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER:
          onMouseOver();
          break;

        case Event.ONMOUSEOUT:
          onMouseOut();
          break;

        case Event.ONMOUSEDOWN:
          if (event.getButton() == Event.BUTTON_LEFT) {
            onMouseDown();
          }
          break;

        case Event.ONCLICK:
          onMouseClick();
          break;
      }
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

    private void setSelected(boolean selected) {
      if (selected) {
        panel.setStyleName(toolbarResources.toolbar().iconButtonPanelSelected());
      }
    }
  }
}
