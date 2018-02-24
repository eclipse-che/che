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
package org.eclipse.che.ide.statepersistance;

import static org.eclipse.che.ide.statepersistance.AppStateConstants.APP_STATE;
import static org.eclipse.che.ide.statepersistance.AppStateConstants.PART_STACKS;
import static org.eclipse.che.ide.statepersistance.AppStateConstants.PERSPECTIVES;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import elemental.json.Json;
import elemental.json.JsonException;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import java.util.Optional;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.statepersistance.StateComponent;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Responsible for persisting and restoring IDE state across sessions. Uses user preferences as
 * storage for serialized state.
 *
 * @author Artem Zatsarynnyi
 * @author Yevhen Vydolob
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AppStateManager {

  /** The name of the property for the mappings in user preferences. */
  public static final String PREFERENCE_PROPERTY_NAME = "IdeAppStates";

  private static final String WORKSPACE = "workspace";

  private final Provider<PerspectiveManager> perspectiveManagerProvider;
  private final Provider<StateComponentRegistry> stateComponentRegistry;

  private final PreferencesManager preferencesManager;
  private final JsonFactory jsonFactory;
  private final PromiseProvider promises;
  private EventBus eventBus;
  private final AppContext appContext;
  private JsonObject allWsState;
  private final DelayedTask persistWorkspaceStateTask =
      new DelayedTask() {
        @Override
        public void onExecute() {
          persistWorkspaceState();
        }
      };

  @Inject
  public AppStateManager(
      Provider<PerspectiveManager> perspectiveManagerProvider,
      Provider<StateComponentRegistry> stateComponentRegistryProvider,
      PreferencesManager preferencesManager,
      JsonFactory jsonFactory,
      PromiseProvider promises,
      EventBus eventBus,
      AppContext appContext) {
    this.perspectiveManagerProvider = perspectiveManagerProvider;
    this.stateComponentRegistry = stateComponentRegistryProvider;
    this.preferencesManager = preferencesManager;
    this.jsonFactory = jsonFactory;
    this.promises = promises;
    this.eventBus = eventBus;
    this.appContext = appContext;

    eventBus.addHandler(WorkspaceReadyEvent.getType(), this::onWorkspaceReady);

    eventBus.addHandler(
        WindowActionEvent.TYPE,
        new WindowActionHandler() {
          @Override
          public void onWindowClosing(WindowActionEvent event) {
            Workspace workspace = appContext.getWorkspace();
            if (workspace != null) {
              persistWorkspaceState();
            }
          }

          @Override
          public void onWindowClosed(WindowActionEvent event) {}
        });
  }

  public void readStateFromPreferences() {
    final String json = preferencesManager.getValue(APP_STATE);
    if (json == null) {
      allWsState = jsonFactory.createObject();
    } else {
      try {
        allWsState = jsonFactory.parse(json);
      } catch (Exception e) {
        // create 'clear' state if any deserializing error occurred
        allWsState = jsonFactory.createObject();
      }
    }
  }

  /**
   * Gets cached state for given {@code partStackType}. Use {@link #readStateFromPreferences()}
   * first to get not cached state
   */
  @Nullable
  public JsonObject getStateFor(PartStackType partStackType) {
    String workspaceId = appContext.getWorkspace().getId();
    if (!allWsState.hasKey(workspaceId)) {
      return null;
    }

    JsonObject preferences = allWsState.getObject(workspaceId);
    if (!preferences.hasKey(WORKSPACE)) {
      return null;
    }

    JsonObject workspace = preferences.getObject(WORKSPACE);
    if (!workspace.hasKey(WORKSPACE)) {
      return null;
    }

    JsonObject workspaceState = workspace.getObject(WORKSPACE);
    if (!workspaceState.hasKey(PERSPECTIVES)) {
      return null;
    }

    String perspectiveId = perspectiveManagerProvider.get().getPerspectiveId();
    JsonObject perspectives = workspaceState.getObject(PERSPECTIVES);
    if (!perspectives.hasKey(perspectiveId)) {
      return null;
    }

    JsonObject projectPerspective = perspectives.getObject(perspectiveId);
    if (!projectPerspective.hasKey(PART_STACKS)) {
      return null;
    }

    JsonObject partStacks = projectPerspective.getObject(PART_STACKS);
    if (!partStacks.hasKey(partStackType.name())) {
      return null;
    }

    return partStacks.getObject(partStackType.name());
  }

  private Promise<Void> restoreWorkspaceStateWithDelay() {
    return AsyncPromiseHelper.createFromAsyncRequest(
        callback ->
            new Timer() {
              @Override
              public void run() {
                restoreWorkspaceState()
                    .then(
                        arg -> {
                          callback.onSuccess(null);
                        })
                    .catchError(
                        arg -> {
                          callback.onFailure(arg.getCause());
                        });
              }
            }.schedule(1000));
  }

  @VisibleForTesting
  Promise<Void> restoreWorkspaceState() {
    final String wsId = appContext.getWorkspace().getId();

    if (allWsState.hasKey(wsId)) {
      return restoreState(allWsState.getObject(wsId));
    }
    return promises.resolve(null);
  }

  private Promise<Void> restoreState(JsonObject settings) {
    try {
      if (settings.hasKey(WORKSPACE)) {
        JsonObject workspace = settings.getObject(WORKSPACE);
        Promise<Void> sequentialRestore = promises.resolve(null);
        for (String key : workspace.keys()) {
          Optional<StateComponent> stateComponent =
              stateComponentRegistry.get().getComponentById(key);
          if (stateComponent.isPresent()) {
            StateComponent component = stateComponent.get();
            Log.debug(getClass(), "Restore state for the component ID: " + component.getId());
            sequentialRestore =
                sequentialRestore.thenPromise(
                    ignored -> component.loadState(workspace.getObject(key)));
          }
        }
        return sequentialRestore;
      }
    } catch (JsonException e) {
      Log.error(getClass(), e);
    }
    return promises.resolve(null);
  }

  public Promise<Void> persistWorkspaceState() {
    String wsId = appContext.getWorkspace().getId();
    JsonObject settings = Json.createObject();
    JsonObject workspace = Json.createObject();
    settings.put(WORKSPACE, workspace);

    for (StateComponent entry : stateComponentRegistry.get().getComponents()) {
      try {
        Log.debug(getClass(), "Persist state for the component ID: " + entry.getId());
        workspace.put(entry.getId(), entry.getState());
      } catch (Exception e) {
        Log.error(getClass(), e);
      }
    }
    JsonObject oldSettings = allWsState.getObject(wsId);
    if (oldSettings == null || !oldSettings.toJson().equals(settings.toJson())) {
      allWsState.put(wsId, settings);
      return writeStateToPreferences(allWsState);
    } else {
      return promises.resolve(null);
    }
  }

  private Promise<Void> writeStateToPreferences(JsonObject state) {
    final String json = state.toJson();
    preferencesManager.setValue(APP_STATE, json);
    return preferencesManager
        .flushPreferences()
        .catchError(
            error -> {
              Log.error(
                  AppStateManager.class,
                  "Failed to store app's state to user's preferences: " + error.getMessage());
            });
  }

  @Deprecated
  public boolean hasStateForWorkspace(String wsId) {
    return allWsState.hasKey(wsId);
  }

  private void onWorkspaceReady(WorkspaceReadyEvent workspaceReadyEvent) {
    // delay is required because we need to wait some time while different components initialized
    restoreWorkspaceStateWithDelay()
        .then(
            ignored -> {
              eventBus.addHandler(
                  PartStackStateChangedEvent.TYPE, event -> persistWorkspaceStateTask.delay(500));
            });
  }
}
