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
package org.eclipse.che.ide.command.toolbar.selector;

import static org.eclipse.che.ide.api.parts.PartStack.State.HIDDEN;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;

/** Presenter to manage Panel selector widget and perspective layout. */
@Singleton
public class PanelSelectorPresenter implements Presenter, PanelSelectorView.ActionDelegate {

  private PanelSelectorView view;

  private PerspectiveManager perspectiveManager;

  @Inject
  public PanelSelectorPresenter(
      PanelSelectorView view, PerspectiveManager perspectiveManager, EventBus eventBus) {
    this.view = view;
    this.perspectiveManager = perspectiveManager;

    view.setDelegate(this);

    eventBus.addHandler(
        PartStackStateChangedEvent.TYPE,
        new PartStackStateChangedEvent.Handler() {
          @Override
          public void onPartStackStateChanged(PartStackStateChangedEvent event) {
            updateButtonState();
          }
        });
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  @Override
  public void onButtonClicked() {
    view.showPopup();
  }

  @Override
  public void onSelectorLeftClicked() {
    showPanels(true, false, false);
  }

  @Override
  public void onSelectorLeftBottomClicked() {
    showPanels(true, true, false);
  }

  @Override
  public void onSelectorFullEditorClicked() {
    showPanels(false, false, false);
  }

  @Override
  public void onSelectorBottomClicked() {
    showPanels(false, true, false);
  }

  @Override
  public void onSelectorRightClicked() {
    showPanels(false, false, true);
  }

  @Override
  public void onSelectorLeftRightBottomClicked() {
    showPanels(true, true, true);
  }

  /**
   * Sets new visibility for left, bottom and right panels.
   *
   * @param left left panel
   * @param bottom bottom panel
   * @param right right panel
   */
  private void showPanels(boolean left, boolean bottom, boolean right) {
    Perspective perspective = perspectiveManager.getActivePerspective();
    if (perspective == null) {
      return;
    }

    PartStack editorPartStack = perspective.getPartStack(PartStackType.EDITING);
    editorPartStack.restore();

    PartStack leftPartStack = perspective.getPartStack(PartStackType.NAVIGATION);
    PartStack bottomPartStack = perspective.getPartStack(PartStackType.INFORMATION);
    PartStack rightPartStack = perspective.getPartStack(PartStackType.TOOLING);

    if (left) {
      if (HIDDEN == leftPartStack.getPartStackState()) {
        leftPartStack.show();
      } else {
        leftPartStack.restore();
      }
    } else {
      leftPartStack.hide();
    }

    if (bottom) {
      if (HIDDEN == bottomPartStack.getPartStackState()) {
        bottomPartStack.show();
      } else {
        bottomPartStack.restore();
      }
    } else {
      bottomPartStack.hide();
    }

    if (right) {
      if (HIDDEN == rightPartStack.getPartStackState()) {
        rightPartStack.show();
      } else {
        rightPartStack.restore();
      }
    } else {
      rightPartStack.hide();
    }

    updateButtonState();
  }

  /** Updates icon for panel selector button displaying the current state of panels. */
  private void updateButtonState() {
    Perspective perspective = perspectiveManager.getActivePerspective();
    if (perspective == null) {
      return;
    }

    PartStack leftPartStack = perspective.getPartStack(PartStackType.NAVIGATION);
    PartStack bottomPartStack = perspective.getPartStack(PartStackType.INFORMATION);
    PartStack rightPartStack = perspective.getPartStack(PartStackType.TOOLING);

    if (leftPartStack == null || bottomPartStack == null || rightPartStack == null) {
      return;
    }

    if (HIDDEN != leftPartStack.getPartStackState()
        && HIDDEN == bottomPartStack.getPartStackState()
        && HIDDEN == rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.LEFT);
    } else if (HIDDEN != leftPartStack.getPartStackState()
        && HIDDEN != bottomPartStack.getPartStackState()
        && HIDDEN == rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.LEFT_BOTTOM);
    } else if (HIDDEN == leftPartStack.getPartStackState()
        && HIDDEN == bottomPartStack.getPartStackState()
        && HIDDEN == rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.FULL_EDITOR);
    } else if (HIDDEN == leftPartStack.getPartStackState()
        && HIDDEN != bottomPartStack.getPartStackState()
        && HIDDEN == rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.BOTTOM);
    } else if (HIDDEN == leftPartStack.getPartStackState()
        && HIDDEN == bottomPartStack.getPartStackState()
        && HIDDEN != rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.RIGHT);
    } else if (HIDDEN != leftPartStack.getPartStackState()
        && HIDDEN != bottomPartStack.getPartStackState()
        && HIDDEN != rightPartStack.getPartStackState()) {
      view.setState(PanelSelectorView.State.LEFT_RIGHT_BOTTOM);
    }
  }
}
