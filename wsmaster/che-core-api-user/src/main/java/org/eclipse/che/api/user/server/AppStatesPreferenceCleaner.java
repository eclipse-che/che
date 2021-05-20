/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handler for clean up app state preference when workspace is removed. */
@Singleton
public class AppStatesPreferenceCleaner implements EventSubscriber<WorkspaceRemovedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(AppStatesPreferenceCleaner.class);

  /** The name of the property for the mappings in user preferences. */
  public static final String APP_STATES_PREFERENCE_PROPERTY = "IdeAppStates";

  private JsonParser jsonParser;
  private EventService eventService;
  private UserManager userManager;
  private PreferenceManager preferenceManager;

  @Inject
  public AppStatesPreferenceCleaner(
      JsonParser jsonParser,
      EventService eventService,
      UserManager userManager,
      PreferenceManager preferenceManager) {
    this.jsonParser = jsonParser;
    this.eventService = eventService;
    this.userManager = userManager;
    this.preferenceManager = preferenceManager;
  }

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent workspaceRemovedEvent) {
    try {
      Workspace workspace = workspaceRemovedEvent.getWorkspace();
      User user = userManager.getByName(workspace.getNamespace());
      if (user == null) {
        return;
      }

      String userId = user.getId();
      Map<String, String> preferences = preferenceManager.find(userId);
      String appStates = preferences.get(APP_STATES_PREFERENCE_PROPERTY);
      if (appStates == null) {
        return;
      }

      JsonObject workspaces = jsonParser.parse(appStates).getAsJsonObject();
      JsonElement removedWorkspacePreferences = workspaces.remove(workspace.getId());
      if (removedWorkspacePreferences != null) {
        preferences.put(APP_STATES_PREFERENCE_PROPERTY, workspaces.toString());
        preferenceManager.save(userId, preferences);
      }
    } catch (NotFoundException | ServerException e) {
      Workspace workspace = workspaceRemovedEvent.getWorkspace();
      LOG.error(
          "Unable to clean up preferences for owner of the workspace {} with namespace {} because of '{}'",
          workspace.getId(),
          workspace.getNamespace(),
          e.getMessage());
    }
  }
}
