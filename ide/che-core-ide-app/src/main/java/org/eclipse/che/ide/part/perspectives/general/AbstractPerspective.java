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
package org.eclipse.che.ide.part.perspectives.general;

import static org.eclipse.che.ide.api.parts.PartStack.State.HIDDEN;
import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveView;
import org.eclipse.che.ide.api.parts.base.MaximizePartEvent;
import org.eclipse.che.ide.part.PartStackPresenterFactory;
import org.eclipse.che.ide.part.PartStackViewFactory;
import org.eclipse.che.ide.part.WorkBenchControllerFactory;
import org.eclipse.che.ide.part.WorkBenchPartController;
import org.eclipse.che.providers.DynaProvider;

/**
 * The class which contains general business logic for all perspectives.
 *
 * @author Dmitry Shnurenko
 */
// TODO need rewrite this, remove direct dependency on PerspectiveViewImpl and other GWT Widgets
public abstract class AbstractPerspective
    implements Presenter,
        Perspective,
        ActivePartChangedHandler,
        PartStackStateChangedEvent.Handler,
        MaximizePartEvent.Handler,
        PerspectiveView.ActionDelegate,
        PartStack.ActionDelegate {

  /** The default size for the part. */
  private static final double DEFAULT_PART_SIZE = 260;

  /** The minimum allowable size for the part. */
  private static final int MIN_PART_SIZE = 100;

  protected final Map<PartStackType, PartStack> partStacks;
  protected final PerspectiveViewImpl view;

  private final String perspectiveId;
  private final DynaProvider dynaProvider;

  private final WorkBenchPartController leftPartController;
  private final WorkBenchPartController rightPartController;
  private final WorkBenchPartController belowPartController;

  private PartPresenter activePart;
  private PartPresenter activePartBeforeChangePerspective;

  private PartStack maximizedPartStack;

  protected AbstractPerspective(
      @NotNull String perspectiveId,
      @NotNull PerspectiveViewImpl view,
      @NotNull PartStackPresenterFactory stackPresenterFactory,
      @NotNull PartStackViewFactory partViewFactory,
      @NotNull WorkBenchControllerFactory controllerFactory,
      @NotNull EventBus eventBus,
      @NotNull DynaProvider dynaProvider) {
    this.view = view;
    this.perspectiveId = perspectiveId;
    this.dynaProvider = dynaProvider;
    this.partStacks = new HashMap<>();

    view.setDelegate(this);

    PartStackView navigationView = partViewFactory.create(view.getLeftPanel());
    leftPartController =
        controllerFactory.createController(view.getSplitPanel(), view.getNavigationPanel());
    PartStack navigationPartStack =
        stackPresenterFactory.create(navigationView, leftPartController);
    navigationPartStack.setDelegate(this);
    partStacks.put(NAVIGATION, navigationPartStack);

    PartStackView informationView = partViewFactory.create(view.getBottomPanel());
    belowPartController =
        controllerFactory.createController(view.getSplitPanel(), view.getInformationPanel());
    PartStack informationStack = stackPresenterFactory.create(informationView, belowPartController);
    informationStack.setDelegate(this);
    partStacks.put(INFORMATION, informationStack);

    PartStackView toolingView = partViewFactory.create(view.getRightPanel());
    rightPartController =
        controllerFactory.createController(view.getSplitPanel(), view.getToolPanel());
    PartStack toolingPartStack = stackPresenterFactory.create(toolingView, rightPartController);
    toolingPartStack.setDelegate(this);
    partStacks.put(TOOLING, toolingPartStack);

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    eventBus.addHandler(PartStackStateChangedEvent.TYPE, this);
    eventBus.addHandler(MaximizePartEvent.TYPE, this);
  }

  /**
   * Opens previous active tab on current perspective.
   *
   * @param partStackType part type on which need open previous active part
   */
  protected void openActivePart(@NotNull PartStackType partStackType) {
    PartStack partStack = partStacks.get(partStackType);
    partStack.openPreviousActivePart();
  }

  @Override
  public void storeState() {
    activePartBeforeChangePerspective = activePart;

    if (activePartBeforeChangePerspective != null) {
      activePartBeforeChangePerspective.storeState();
    }
  }

  @Override
  public void restoreState() {
    if (activePartBeforeChangePerspective != null) {
      setActivePart(activePartBeforeChangePerspective);
      activePartBeforeChangePerspective.restoreState();
    }
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    activePart = event.getActivePart();
  }

  @Override
  public void onPartStackStateChanged(PartStackStateChangedEvent event) {
    PartStack changedPartStack = event.getPartStack();
    if (HIDDEN == changedPartStack.getPartStackState()
        && activePart != null
        && activePart.getPartStack() == changedPartStack) {
      activePart = null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removePart(@NotNull PartPresenter part) {
    PartStack destPartStack = findPartStackByPart(part);
    if (destPartStack != null) {
      destPartStack.removePart(part);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void hidePart(@NotNull PartPresenter part) {
    PartStack destPartStack = findPartStackByPart(part);
    if (destPartStack != null) {
      destPartStack.hide();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void maximizeCentralPartStack() {
    onMaximize(partStacks.get(EDITING));
  }

  /** {@inheritDoc} */
  @Override
  public void maximizeLeftPartStack() {
    onMaximize(partStacks.get(NAVIGATION));
  }

  /** {@inheritDoc} */
  @Override
  public void maximizeRightPartStack() {
    onMaximize(partStacks.get(TOOLING));
  }

  /** {@inheritDoc} */
  @Override
  public void maximizeBottomPartStack() {
    onMaximize(partStacks.get(INFORMATION));
  }

  @Override
  public void onMaximizePart(MaximizePartEvent event) {
    PartStack partStack = findPartStackByPart(event.getPart());
    if (partStack == null) {
      return;
    }

    if (partStack.getPartStackState() == PartStack.State.MAXIMIZED) {
      onRestore(partStack);
    } else {
      onMaximize(partStack);
    }
  }

  @Override
  public void onMaximize(PartStack partStack) {
    if (partStack == null) {
      return;
    }

    if (partStack.equals(maximizedPartStack)) {
      return;
    }

    maximizedPartStack = partStack;

    for (PartStack ps : partStacks.values()) {
      if (!ps.equals(partStack)) {
        ps.minimize();
      }
    }

    partStack.maximize();
  }

  @Override
  public void onRestore(PartStack partStack) {
    for (PartStack ps : partStacks.values()) {
      ps.restore();
    }

    maximizedPartStack = null;
  }

  /** {@inheritDoc} */
  @Override
  public void restore() {
    onRestore(null);
  }

  /** {@inheritDoc} */
  @Override
  public void setActivePart(@NotNull PartPresenter part) {
    PartStack destPartStack = findPartStackByPart(part);
    if (destPartStack != null) {
      destPartStack.setActivePart(part);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setActivePart(@NotNull PartPresenter part, @NotNull PartStackType type) {
    PartStack destPartStack = partStacks.get(type);
    destPartStack.setActivePart(part);
  }

  /**
   * Find parent PartStack for given Part
   *
   * @param part part for which need find parent
   * @return Parent PartStackPresenter or null if part not registered
   */
  public PartStack findPartStackByPart(@NotNull PartPresenter part) {
    for (PartStackType partStackType : PartStackType.values()) {
      if (partStacks.get(partStackType).containsPart(part)) {
        return partStacks.get(partStackType);
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void addPart(@NotNull PartPresenter part, @NotNull PartStackType type) {
    addPart(part, type, null);
  }

  /** {@inheritDoc} */
  @Override
  public void addPart(
      @NotNull PartPresenter part, @NotNull PartStackType type, @Nullable Constraints constraint) {
    PartStack destPartStack = partStacks.get(type);

    List<String> rules = part.getRules();
    if (rules.isEmpty() || rules.contains(perspectiveId)) {
      destPartStack.addPart(part, constraint);
    }
  }

  @Override
  public void onResize(int width, int height) {
    if (maximizedPartStack != null) {
      maximizedPartStack.maximize();
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public PartStack getPartStack(@NotNull PartStackType type) {
    return partStacks.get(type);
  }

  @Override
  @Nullable
  public PartPresenter getActivePart() {
    return activePart;
  }

  @Override
  public JsonObject getState() {
    JsonObject state = Json.createObject();
    JsonObject partStacks = Json.createObject();
    if (activePart != null) {
      state.put("ACTIVE_PART", activePart.getClass().getName());
    }
    state.put("PART_STACKS", partStacks);

    partStacks.put(
        PartStackType.INFORMATION.name(),
        getPartStackState(this.partStacks.get(INFORMATION), belowPartController));
    partStacks.put(
        PartStackType.NAVIGATION.name(),
        getPartStackState(this.partStacks.get(NAVIGATION), leftPartController));
    partStacks.put(
        PartStackType.TOOLING.name(),
        getPartStackState(this.partStacks.get(TOOLING), rightPartController));

    return state;
  }

  private JsonObject getPartStackState(
      PartStack partStack, WorkBenchPartController partController) {
    JsonObject state = Json.createObject();
    state.put("SIZE", partController.getSize());
    state.put("STATE", partStack.getPartStackState().name());

    if (partStack.getParts().isEmpty()) {
      state.put("HIDDEN", true);
    } else {
      if (partStack.getActivePart() != null) {
        state.put("ACTIVE_PART", partStack.getActivePart().getClass().getName());
      }

      state.put("HIDDEN", partController.isHidden());

      JsonArray parts = Json.createArray();
      state.put("PARTS", parts);
      int i = 0;
      for (PartPresenter entry : partStack.getParts()) {
        JsonObject presenterState = Json.createObject();
        presenterState.put("CLASS", entry.getClass().getName());
        parts.set(i++, presenterState);
      }
    }
    return state;
  }

  @Override
  public Promise<Void> loadState(@NotNull JsonObject state) {
    if (state.hasKey("PART_STACKS")) {
      JsonObject partStacksState = state.getObject("PART_STACKS");

      // Don't restore part dimensions if perspective is maximized.
      boolean perspectiveMaximized = isPerspectiveMaximized(partStacksState);

      for (String partStackType : partStacksState.keys()) {
        JsonObject partStackState = partStacksState.getObject(partStackType);
        switch (PartStackType.valueOf(partStackType)) {
          case INFORMATION:
            loadPartStackState(
                partStacks.get(INFORMATION),
                belowPartController,
                partStackState,
                perspectiveMaximized);
            break;
          case NAVIGATION:
            loadPartStackState(
                partStacks.get(NAVIGATION),
                leftPartController,
                partStackState,
                perspectiveMaximized);
            break;
          case TOOLING:
            loadPartStackState(
                partStacks.get(TOOLING), rightPartController, partStackState, perspectiveMaximized);
            break;
          default:
        }
      }
    }

    // restore perspective's active part
    if (state.hasKey("ACTIVE_PART")) {
      String activePart = state.getString("ACTIVE_PART");
      Provider<PartPresenter> provider = dynaProvider.getProvider(activePart);
      if (provider != null) {
        setActivePart(provider.get());
      }
    }

    return Promises.resolve(null);
  }

  /**
   * Determines whether perspective is maximized.
   *
   * @param partStacksState part stack state
   * @return <b>true</b> is perspective has maximized part stack
   */
  private boolean isPerspectiveMaximized(JsonObject partStacksState) {
    for (String partStackType : partStacksState.keys()) {
      JsonObject partStackState = partStacksState.getObject(partStackType);
      if (partStackState.hasKey("STATE")
          && PartStack.State.MAXIMIZED.name().equals(partStackState.getString("STATE"))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Set part stack state.
   *
   * @param partStack
   * @param controller
   * @param partStackState
   * @param skipRestoreDimensions
   */
  private void loadPartStackState(
      PartStack partStack,
      WorkBenchPartController controller,
      JsonObject partStackState,
      boolean skipRestoreDimensions) {
    if (partStackState.hasKey("PARTS")) {
      JsonArray parts = partStackState.get("PARTS");

      for (int i = 0; i < parts.length(); i++) {
        JsonObject value = parts.get(i);
        if (value.hasKey("CLASS")) {
          String className = value.getString("CLASS");
          Provider<PartPresenter> provider = dynaProvider.getProvider(className);
          if (provider != null) {
            PartPresenter partPresenter = provider.get();
            if (!partStack.containsPart(partPresenter)) {
              partStack.addPart(partPresenter);
            }
          }
        }
      }
    }

    // restore part stack's active part
    if (partStackState.hasKey("ACTIVE_PART")) {
      String activePart = partStackState.getString("ACTIVE_PART");
      Provider<PartPresenter> provider = dynaProvider.getProvider(activePart);
      if (provider != null) {
        partStack.setActivePart(provider.get());
      }
    }

    // hide part stack if it has no parts
    if (partStack.getParts().isEmpty()) {
      controller.setHidden(true);
      return;
    }

    if (skipRestoreDimensions) {
      return;
    }

    if (partStackState.hasKey("HIDDEN") && partStackState.getBoolean("HIDDEN")) {
      partStack.hide();
      return;
    }

    if (partStackState.hasKey("SIZE")) {
      double size = partStackState.getNumber("SIZE");

      // Size of the part must not be less 100 pixels.
      if (size <= MIN_PART_SIZE) {
        size = DEFAULT_PART_SIZE;
      }

      controller.setSize(size);
    }
  }

  @Override
  public String getId() {
    return null;
  }
}
