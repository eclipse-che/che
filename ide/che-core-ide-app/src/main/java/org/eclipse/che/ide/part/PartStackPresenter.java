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
package org.eclipse.che.ide.part;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.events.EditorDirtyStateChangedEvent;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.parts.PropertyListener;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.menu.PartMenu;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;

/**
 * Implements "Tab-like" UI Component, that accepts PartPresenters as child elements.
 *
 * <p>PartStack support "focus" (please don't mix with GWT Widget's Focus feature). Focused
 * PartStack will highlight active Part, notifying user what component is currently active.
 *
 * @author Nikolay Zamosenchuk
 * @author Stéphane Daviet
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class PartStackPresenter
    implements Presenter, PartStackView.ActionDelegate, PartButton.ActionDelegate, PartStack {

  /** The default size for the part. */
  private static final double DEFAULT_PART_SIZE = 260;

  /** The minimum allowable size for the part. */
  private static final int MIN_PART_SIZE = 100;

  private final WorkBenchPartController workBenchPartController;
  private final PartsComparator partsComparator;
  private final Map<PartPresenter, Constraints> constraints;
  private final PartStackEventHandler partStackHandler;
  private final EventBus eventBus;
  private final PartMenu partMenu;

  protected final Map<TabItem, PartPresenter> parts;
  protected final TabItemFactory tabItemFactory;
  protected final PartStackView view;
  protected final PropertyListener propertyListener;

  protected PartPresenter activePart;
  protected TabItem activeTab;

  private TabItem previousActiveTab;
  private PartPresenter previousActivePart;

  protected double currentSize;

  private State state = State.HIDDEN;

  private ActionDelegate delegate;

  @Inject
  public PartStackPresenter(
      final EventBus eventBus,
      final PartMenu partMenu,
      PartStackEventHandler partStackEventHandler,
      TabItemFactory tabItemFactory,
      PartsComparator partsComparator,
      @Assisted final PartStackView view,
      @Assisted @NotNull WorkBenchPartController workBenchPartController) {
    this.view = view;
    this.view.setDelegate(this);

    this.eventBus = eventBus;
    this.partMenu = partMenu;
    this.partStackHandler = partStackEventHandler;
    this.workBenchPartController = workBenchPartController;
    this.tabItemFactory = tabItemFactory;
    this.partsComparator = partsComparator;

    this.parts = new HashMap<>();
    this.constraints = new LinkedHashMap<>();

    this.propertyListener =
        new PropertyListener() {
          @Override
          public void propertyChanged(PartPresenter source, int propId) {
            if (PartPresenter.TITLE_PROPERTY == propId) {
              updatePartTab(source);
            } else if (EditorPartPresenter.PROP_DIRTY == propId) {
              eventBus.fireEvent(new EditorDirtyStateChangedEvent((EditorPartPresenter) source));
            }
          }
        };

    if (workBenchPartController != null) {
      this.workBenchPartController.setSize(DEFAULT_PART_SIZE);
    }

    currentSize = DEFAULT_PART_SIZE;
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  private void updatePartTab(@NotNull PartPresenter part) {
    if (!containsPart(part)) {
      return;
    }

    view.updateTabItem(part);
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  /** {@inheritDoc} */
  @Override
  public void addPart(@NotNull PartPresenter part) {
    addPart(part, null);
  }

  /** {@inheritDoc} */
  @Override
  public void addPart(@NotNull PartPresenter part, @Nullable Constraints constraint) {
    if (containsPart(part)) {
      TabItem tab = getTabByPart(part);
      onTabClicked(tab);
      return;
    }

    if (part instanceof BasePresenter) {
      ((BasePresenter) part).setPartStack(this);
    }

    part.addPropertyListener(propertyListener);

    PartButton partButton =
        tabItemFactory
            .createPartButton(part.getTitle())
            .setTooltip(part.getTitleToolTip())
            .setIcon(part.getTitleImage());
    partButton.setDelegate(this);

    parts.put(partButton, part);
    constraints.put(part, constraint);

    view.addTab(partButton, part);

    sortPartsOnView();

    onRequestFocus();
  }

  private void sortPartsOnView() {
    List<PartPresenter> sortedParts = new ArrayList<>();
    sortedParts.addAll(parts.values());
    partsComparator.setConstraints(constraints);

    Collections.sort(sortedParts, partsComparator);

    view.setTabPositions(sortedParts);
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsPart(PartPresenter part) {
    return parts.values().contains(part);
  }

  /** {@inheritDoc} */
  @Override
  public PartPresenter getActivePart() {
    return activePart;
  }

  /** {@inheritDoc} */
  @Override
  public void setActivePart(@NotNull PartPresenter part) {
    TabItem tab = getTabByPart(part);
    if (tab == null) {
      return;
    }

    activePart = part;
    activeTab = tab;

    if (state == State.HIDDEN) {
      state = State.NORMAL;

      if (currentSize < MIN_PART_SIZE) {
        currentSize = DEFAULT_PART_SIZE;
      }

      workBenchPartController.setSize(currentSize);
      workBenchPartController.setHidden(false);

    } else if (state == State.MINIMIZED) {
      // Minimized state means the other part stack is maximized.
      // Ask the delegate to restore part stacks.
      if (delegate != null) {
        delegate.onRestore(this);
      }

    } else if (state == State.NORMAL) {
      if (workBenchPartController.getSize() < MIN_PART_SIZE) {
        workBenchPartController.setSize(DEFAULT_PART_SIZE);
      }

      workBenchPartController.setHidden(false);
    }

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });

    selectActiveTab(tab);
  }

  @Nullable
  protected TabItem getTabByPart(@NotNull PartPresenter part) {
    for (Map.Entry<TabItem, PartPresenter> entry : parts.entrySet()) {

      if (part.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void removePart(PartPresenter part) {
    parts.remove(getTabByPart(part));

    view.removeTab(part);
  }

  /** {@inheritDoc} */
  @Override
  public void openPreviousActivePart() {
    if (activePart == null) {
      return;
    }

    TabItem selectedTab = getTabByPart(activePart);
    if (selectedTab != null) {
      selectActiveTab(selectedTab);
    }
  }

  @Override
  public void updateStack() {
    for (PartPresenter partPresenter : parts.values()) {
      if (partPresenter instanceof BasePresenter) {
        ((BasePresenter) partPresenter).setPartStack(this);
      }
    }
  }

  @Override
  public List<? extends PartPresenter> getParts() {
    return new ArrayList<>(constraints.keySet());
  }

  /** {@inheritDoc} */
  @Override
  public void onRequestFocus() {
    partStackHandler.onRequestFocus(PartStackPresenter.this);
  }

  /** {@inheritDoc} */
  @Override
  public void setFocus(boolean focused) {
    view.setFocus(focused);
  }

  @Override
  public void onToggleMaximize() {
    if (getPartStackState() == PartStack.State.MAXIMIZED) {
      restore();
    } else {
      maximize();
    }
  }

  @Override
  public void onHide() {
    hide();
  }

  @Override
  public void maximize() {
    // Update the view state.
    view.setMaximized(true);

    // Update dimensions of the part stack if it's already maximized. Used when resizing the view.
    if (state == State.MAXIMIZED) {
      workBenchPartController.maximize();
      return;
    }

    // Part stack can be maximized only having NORMAL state.
    if (state != State.NORMAL) {
      return;
    }

    // Maximize and update the state.
    currentSize = workBenchPartController.getSize();
    workBenchPartController.maximize();
    state = State.MAXIMIZED;

    // Ask the delegate to maximize this part stack and collapse other.
    if (delegate != null) {
      delegate.onMaximize(this);
    }

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });
  }

  @Override
  public void minimize() {
    // Update the view state.
    view.setMaximized(false);

    // Part stack can be collapsed only having NORMAL state.
    if (state != State.NORMAL) {
      return;
    }

    // Collapse and update the state.
    currentSize = workBenchPartController.getSize();
    workBenchPartController.setSize(0);
    workBenchPartController.setHidden(true);
    state = State.MINIMIZED;

    // Deselect the active tab.
    if (activeTab != null) {
      activeTab.unSelect();
    }

    previousActiveTab = activeTab;
    previousActivePart = activePart;

    activeTab = null;
    activePart = null;

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });
  }

  @Override
  public State getPartStackState() {
    return state;
  }

  @Override
  public void hide() {
    // Update the view state.
    view.setMaximized(false);

    // Ask the delegate to restore pack stack if it's maximized.
    if (state == State.MAXIMIZED) {
      if (delegate != null) {
        delegate.onRestore(this);
      }
    }

    // Part stack can be minimized only having NORMAL state.
    if (state == State.NORMAL) {
      currentSize = workBenchPartController.getSize();
      workBenchPartController.setSize(0);
      workBenchPartController.setHidden(true);
      state = State.HIDDEN;
    }

    // Deselect active tab.
    if (activeTab != null) {
      activeTab.unSelect();
    }

    previousActiveTab = activeTab;
    previousActivePart = activePart;

    activeTab = null;
    activePart = null;

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });
  }

  @Override
  public void show() {
    if (state != State.HIDDEN) {
      return;
    }

    // Change the state to MINIMIZED for the following restoring.
    state = State.MINIMIZED;

    if (delegate != null) {
      delegate.onRestore(this);
    }

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });

    if (activePart != null) {
      activePart.onOpen();
    }

    if (activeTab != null) {
      selectActiveTab(activeTab);
    }
  }

  @Override
  public void restore() {
    // Update the view state.
    view.setMaximized(false);

    // Don't restore part stack if it's in MINIMIZED or NORMAL state.
    if (state == State.HIDDEN || state == State.NORMAL) {
      return;
    }

    // Restore and update the stack.
    State prevState = state;
    state = State.NORMAL;

    if (currentSize < MIN_PART_SIZE) {
      currentSize = DEFAULT_PART_SIZE;
    }

    workBenchPartController.setSize(currentSize);
    workBenchPartController.setHidden(false);

    // Ask the delegate to restore part stacks if this part stack was maximized.
    if (prevState == State.MAXIMIZED) {
      if (delegate != null) {
        delegate.onRestore(this);
      }
    }

    if (State.MINIMIZED == prevState) {
      activeTab = previousActiveTab;
      activePart = previousActivePart;

      previousActiveTab = null;
      previousActivePart = null;
    }

    // Select active tab.
    if (activeTab != null) {
      activeTab.select();
    }

    // Notify the part stack state has been changed.
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                eventBus.fireEvent(new PartStackStateChangedEvent(PartStackPresenter.this));
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onTabClicked(@NotNull TabItem selectedTab) {
    // Minimize the part stack if user clicked on the active tab.
    if (selectedTab.equals(activeTab)) {
      return;
    }

    // Change active tab.
    activeTab = selectedTab;
    activePart = parts.get(selectedTab);
    activePart.onOpen();
    selectActiveTab(activeTab);
  }

  private void selectActiveTab(@NotNull TabItem selectedTab) {
    workBenchPartController.setHidden(false);

    PartPresenter selectedPart = parts.get(selectedTab);
    view.selectTab(selectedPart);
  }

  @Override
  public void onPartStackMenu(int mouseX, int mouseY) {
    partMenu.show(mouseX, mouseY);
  }

  /** Handles PartStack actions */
  public interface PartStackEventHandler {
    /** PartStack is being clicked and requests Focus */
    void onRequestFocus(PartStack partStack);
  }
}
