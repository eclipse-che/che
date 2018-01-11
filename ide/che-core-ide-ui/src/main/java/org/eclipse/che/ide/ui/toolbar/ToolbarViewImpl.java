/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;

/**
 * The implementation of {@link ToolbarView}
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public class ToolbarViewImpl extends FlowPanel implements ToolbarView {

  public static final int DELAY_MILLIS = 1000;
  private final Timer timer;
  private FlowPanel leftToolbar;
  private FlowPanel centerToolbar;
  private FlowPanel rightToolbar;
  private ActionGroup leftActionGroup;
  private ActionGroup centerActionGroup;
  private ActionGroup rightActionGroup;
  private ActionManager actionManager;
  private KeyBindingAgent keyBindingAgent;
  private List<Utils.VisibleActionGroup> leftVisibleGroupActions;
  private List<Utils.VisibleActionGroup> centerVisibleGroupActions;
  private List<Utils.VisibleActionGroup> rightVisibleGroupActions;
  private Provider<PerspectiveManager> managerProvider;
  private PresentationFactory presentationFactory;
  private boolean addSeparatorFirst;
  private ToolbarResources toolbarResources;
  private ActionDelegate delegate;

  /** Create view with given instance of resources. */
  @Inject
  public ToolbarViewImpl(
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      ToolbarResources toolbarResources,
      Provider<PerspectiveManager> managerProvider) {
    this.actionManager = actionManager;
    this.keyBindingAgent = keyBindingAgent;
    this.managerProvider = managerProvider;
    this.toolbarResources = toolbarResources;

    toolbarResources.toolbar().ensureInjected();

    setStyleName(toolbarResources.toolbar().toolbarPanel());

    leftVisibleGroupActions = new ArrayList<>();
    centerVisibleGroupActions = new ArrayList<>();
    rightVisibleGroupActions = new ArrayList<>();
    presentationFactory = new PresentationFactory();
    leftToolbar = new FlowPanel();
    centerToolbar = new FlowPanel();
    rightToolbar = new FlowPanel();
    timer =
        new Timer() {
          @Override
          public void run() {
            updateActions();
            schedule(DELAY_MILLIS);
          }
        };

    leftToolbar.addStyleName(toolbarResources.toolbar().leftToolbarPart());
    add(leftToolbar);

    centerToolbar.addStyleName(toolbarResources.toolbar().centerToolbarPart());
    add(centerToolbar);

    rightToolbar.addStyleName(toolbarResources.toolbar().rightToolbarPart());
    add(rightToolbar);

    if (!timer.isRunning()) {
      timer.schedule(DELAY_MILLIS);
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setLeftActionGroup(@NotNull ActionGroup leftActionGroup) {
    this.leftActionGroup = leftActionGroup;
  }

  @Override
  public void setCenterActionGroup(@NotNull ActionGroup centerActionGroup) {
    this.centerActionGroup = centerActionGroup;
  }

  @Override
  public void setRightActionGroup(@NotNull ActionGroup rightActionGroup) {
    this.rightActionGroup = rightActionGroup;
  }

  /** Update toolbar if visible actions are changed. */
  private void updateActions() {
    if (leftActionGroup != null) {
      List<Utils.VisibleActionGroup> newLeftVisibleGroupActions =
          Utils.renderActionGroup(leftActionGroup, presentationFactory, actionManager);
      if (newLeftVisibleGroupActions != null
          && !leftVisibleGroupActions.equals(newLeftVisibleGroupActions)) {
        leftVisibleGroupActions = newLeftVisibleGroupActions;
        leftToolbar.clear();
        leftToolbar.add(createToolbarPart(leftVisibleGroupActions));
      }
    }
    if (centerActionGroup != null) {
      List<Utils.VisibleActionGroup> newCenterVisibleGroupActions =
          Utils.renderActionGroup(centerActionGroup, presentationFactory, actionManager);
      if (newCenterVisibleGroupActions != null
          && !centerVisibleGroupActions.equals(newCenterVisibleGroupActions)) {
        centerVisibleGroupActions = newCenterVisibleGroupActions;
        centerToolbar.clear();
        centerToolbar.add(createToolbarPart(centerVisibleGroupActions));
      }
    }
    if (rightActionGroup != null) {
      List<Utils.VisibleActionGroup> newRightVisibleGroupActions =
          Utils.renderActionGroup(rightActionGroup, presentationFactory, actionManager);
      if (newRightVisibleGroupActions != null
          && !rightVisibleGroupActions.equals(newRightVisibleGroupActions)) {
        rightVisibleGroupActions = newRightVisibleGroupActions;
        rightToolbar.clear();
        rightToolbar.add(createToolbarPart(rightVisibleGroupActions));
      }
    }
  }

  /**
   * Creates a toolbar part widget.
   *
   * @return widget
   */
  private Widget createToolbarPart(List<Utils.VisibleActionGroup> visibleActionGroupList) {
    FlowPanel toolbarPart = new FlowPanel();

    if (addSeparatorFirst) {
      final Widget firstDelimiter = createDelimiter();
      toolbarPart.add(firstDelimiter);
    }

    for (Utils.VisibleActionGroup visibleActionGroup : visibleActionGroupList) {
      List<Action> actions = visibleActionGroup.getActionList();
      if (actions == null || actions.size() == 0) {
        continue;
      }
      FlowPanel actionGroupPanel = new FlowPanel();
      actionGroupPanel.setStyleName(toolbarResources.toolbar().toolbarActionGroupPanel());
      toolbarPart.add(actionGroupPanel);
      for (Action action : actions) {
        if (action instanceof Separator) {
          int actionIndex = actions.indexOf(action);
          if (actionIndex > 0 && actionIndex < actions.size() - 1) {
            final Widget delimiter = createDelimiter();
            actionGroupPanel.add(delimiter);
          }
        } else if (action instanceof CustomComponentAction) {
          Presentation presentation = presentationFactory.getPresentation(action);
          Widget customComponent =
              ((CustomComponentAction) action).createCustomComponent(presentation);
          actionGroupPanel.add(customComponent);
        } else if (action instanceof ActionGroup && ((ActionGroup) action).isPopup()) {
          ActionPopupButton button =
              new ActionPopupButton(
                  (ActionGroup) action,
                  actionManager,
                  keyBindingAgent,
                  presentationFactory,
                  managerProvider,
                  toolbarResources);
          actionGroupPanel.add(button);
        } else {
          final ActionButton button = createToolbarButton(action);
          actionGroupPanel.add(button);
        }
      }
    }
    return toolbarPart;
  }

  /**
   * Creates a delimiter widget.
   *
   * @return widget
   */
  private Widget createDelimiter() {
    FlowPanel delimiter = new FlowPanel();
    delimiter.setStyleName(toolbarResources.toolbar().toolbarDelimiter());
    return delimiter;
  }

  /**
   * Creates a toolbar button.
   *
   * @return ActionButton
   */
  private ActionButton createToolbarButton(Action action) {
    return new ActionButton(
        action, actionManager, presentationFactory.getPresentation(action), toolbarResources);
  }

  @Override
  public void setAddSeparatorFirst(boolean addSeparatorFirst) {
    this.addSeparatorFirst = addSeparatorFirst;
  }
}
