/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.workspace;

import static org.eclipse.che.ide.statepersistance.AppStateConstants.PERSPECTIVES;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.util.ArrayOf;
import elemental.util.Collections;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.parts.PerspectiveManager.PerspectiveTypeListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.eclipse.che.ide.menu.MainMenuPresenter;
import org.eclipse.che.ide.menu.StatusPanelGroupPresenter;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;

/**
 * Root Presenter that implements Workspace logic. Descendant Presenters are injected via
 * constructor and exposed to corresponding UI containers. It contains Menu, Toolbar and WorkBench
 * Presenter to expose their views into corresponding places and to maintain their interactions.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
public class WorkspacePresenter
    implements Presenter,
        WorkspaceView.ActionDelegate,
        WorkspaceAgent,
        PerspectiveTypeListener,
        StateComponent {

  private final WorkspaceView view;
  private final String defaultPerspectiveId;
  private final PromiseProvider promises;
  private final MainMenuPresenter mainMenu;
  private final StatusPanelGroupPresenter bottomMenu;
  private final ToolbarPresenter toolbarPresenter;
  private final Provider<PerspectiveManager> perspectiveManagerProvider;

  private Perspective activePerspective;

  @Inject
  public WorkspacePresenter(
      WorkspaceView view,
      Provider<PerspectiveManager> perspectiveManagerProvider,
      MainMenuPresenter mainMenu,
      StatusPanelGroupPresenter bottomMenu,
      @MainToolbar ToolbarPresenter toolbarPresenter,
      @Named("defaultPerspectiveId") String defaultPerspectiveId,
      PromiseProvider promises) {
    this.view = view;
    this.defaultPerspectiveId = defaultPerspectiveId;
    this.promises = promises;
    this.view.setDelegate(this);

    this.toolbarPresenter = toolbarPresenter;
    this.mainMenu = mainMenu;
    this.bottomMenu = bottomMenu;

    this.perspectiveManagerProvider = perspectiveManagerProvider;
    perspectiveManagerProvider.get().addListener(this);

    onPerspectiveChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    mainMenu.go(view.getMenuPanel());
    toolbarPresenter.go(view.getToolbarPanel());
    bottomMenu.go(view.getStatusPanel());

    container.setWidget(view);
  }

  /** {@inheritDoc} */
  @Override
  public void onPerspectiveChanged() {
    activePerspective = perspectiveManagerProvider.get().getActivePerspective();

    if (activePerspective == null) {
      throw new IllegalStateException(
          "Current perspective isn't defined "
              + perspectiveManagerProvider.get().getPerspectiveId());
    }

    activePerspective.go(view.getPerspectivePanel());
  }

  public void setActivePart(@NotNull PartPresenter part, @NotNull PartStackType type) {
    activePerspective.setActivePart(part, type);
  }

  /** {@inheritDoc} */
  @Override
  public void setActivePart(PartPresenter part) {
    activePerspective.setActivePart(part);
  }

  /** {@inheritDoc} */
  @Override
  public void openPart(PartPresenter part, PartStackType type) {
    openPart(part, type, null);
  }

  /** {@inheritDoc} */
  @Override
  public void openPart(PartPresenter part, PartStackType type, Constraints constraint) {
    activePerspective.addPart(part, type, constraint);
  }

  /** {@inheritDoc} */
  @Override
  public void hidePart(PartPresenter part) {
    activePerspective.hidePart(part);
  }

  /** {@inheritDoc} */
  @Override
  public void removePart(PartPresenter part) {
    activePerspective.removePart(part);
  }

  /**
   * Retrieves the instance of the {@link org.eclipse.che.ide.api.parts.PartStack} for given {@link
   * PartStackType}
   *
   * @param type one of the enumerated type {@link org.eclipse.che.ide.api.parts.PartStackType}
   * @return the part stack found, else null
   */
  public PartStack getPartStack(PartStackType type) {
    return activePerspective.getPartStack(type);
  }

  @Override
  @Nullable
  public PartPresenter getActivePart() {
    return activePerspective.getActivePart();
  }

  @Override
  public JsonObject getState() {
    JsonObject state = Json.createObject();
    JsonObject perspectivesJs = Json.createObject();
    state.put(PERSPECTIVES, perspectivesJs);
    Map<String, Perspective> perspectives = perspectiveManagerProvider.get().getPerspectives();
    for (Map.Entry<String, Perspective> entry : perspectives.entrySet()) {
      // store only default perspective
      if (entry.getKey().equals(defaultPerspectiveId)) {
        perspectivesJs.put(entry.getKey(), entry.getValue().getState());
      }
    }
    return state;
  }

  @Override
  public Promise<Void> loadState(JsonObject state) {
    if (state.hasKey(PERSPECTIVES)) {
      JsonObject perspectives = state.getObject(PERSPECTIVES);
      Map<String, Perspective> perspectiveMap = perspectiveManagerProvider.get().getPerspectives();
      ArrayOf<Promise<?>> perspectivePromises = Collections.arrayOf();
      for (String key : perspectives.keys()) {
        if (perspectiveMap.containsKey(key)) {
          perspectivePromises.push(perspectiveMap.get(key).loadState(perspectives.getObject(key)));
        }
      }
      return promises.all2(perspectivePromises).thenPromise(ignored -> promises.resolve(null));
    }

    return promises.resolve(null);
  }

  @Override
  public String getId() {
    return "workspace";
  }
}
