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

package org.eclipse.che.ide.js.impl.action;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.Disposable;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.js.api.action.ActionManager;
import org.eclipse.che.ide.js.api.action.PerformAction;
import org.eclipse.che.ide.js.api.action.UpdateAction;
import org.eclipse.che.ide.js.api.context.PluginContext;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;
import org.eclipse.che.ide.js.plugin.model.PluginManifest;

/** @author Yevhen Vydolob */
@Singleton
public class JsActionManager implements ActionManager {

  private final org.eclipse.che.ide.api.action.ActionManager actionManager;
  private final ImageRegistry imageRegistry;

  @Inject
  public JsActionManager(
      org.eclipse.che.ide.api.action.ActionManager actionManager, ImageRegistry imageRegistry) {
    this.actionManager = actionManager;
    this.imageRegistry = imageRegistry;
  }

  @Override
  public Disposable registerAction(
      String actionId, UpdateAction updateAction, PerformAction performAction) {

    Action action = actionManager.getAction(actionId);
    if (action instanceof JsAction) {
      JsAction jsAction = (JsAction) action;
      jsAction.setUpdateAction(updateAction);
      jsAction.setPerformAction(performAction);
    }
    return () -> {
      if (actionManager.hasAction(actionId)) {
        actionManager.unregisterAction(actionId);
      }
    };
  }

  public void registerPluginActions(
      List<PluginManifest> plugins, Map<String, PluginContext> activePlugins) {
    for (PluginManifest plugin : plugins) {
      if (plugin.getContributions().getActions() != null) {
        PluginContext context = activePlugins.get(plugin.getPluginId());
        handlePluginActions(plugin.getContributions().getActions(), plugin.getPluginId(), context);
      }
    }
  }

  private void handlePluginActions(JsonArray actions, String pluginId, PluginContext context) {
    for (int i = 0; i < actions.length(); i++) {
      JsonObject object = actions.getObject(i);
      if (object.hasKey("action")) {
        handleAction(object.getObject("action"), pluginId, context);
      } else if (object.hasKey("group")) {
        handleGroup(object.getObject("group"), pluginId, context);
      }
      // ignore others
    }
  }

  private Action handleGroup(JsonObject group, String pluginId, PluginContext context) {
    String id = group.getString("id");
    JsActionGroup actionGroup = new JsActionGroup(actionManager, imageRegistry);
    actionManager.registerAction(id, actionGroup, pluginId);
    context.addDisposable(
        () -> {
          if (actionManager.hasAction(id)) {
            actionManager.unregisterAction(id);
          }
        });
    Presentation presentation = actionGroup.getTemplatePresentation();
    if (group.hasKey("text")) {
      presentation.setText(group.getString("text"));
    }
    if (group.hasKey("description")) {
      presentation.setDescription(group.getString("description"));
    }

    if (group.hasKey("icon")) {
      actionGroup.setImageId(group.getString("icon"));
    }

    if (group.hasKey("popup")) {
      actionGroup.setPopup(group.getBoolean("popup"));
    }

    if (group.hasKey("addToGroup")) {
      addActionToGroup(actionGroup, group.getObject("addToGroup"), context);
    }

    if (group.hasKey("actions")) {
      handleGroupActions(actionGroup, group.getArray("actions"), pluginId, context);
    }
    return actionGroup;
  }

  private void handleGroupActions(
      JsActionGroup parentGroup, JsonArray actions, String pluginId, PluginContext context) {
    for (int i = 0; i < actions.length(); i++) {
      JsonValue jsonValue = actions.get(i);
      if (jsonValue.getType() == JsonType.STRING) {
        if (jsonValue.asString().equals("separator")) {
          parentGroup.addSeparator();
        }
      } else if (jsonValue.getType() == JsonType.OBJECT) {
        JsonObject item = (JsonObject) jsonValue;
        if (item.hasKey("action")) {
          Action action = handleAction(item.getObject("action"), pluginId, context);
          parentGroup.add(action);

          context.addDisposable(() -> parentGroup.remove(action));

        } else if (item.hasKey("group")) {
          Action group = handleGroup(item.getObject("group"), pluginId, context);
          parentGroup.add(group);
          context.addDisposable(() -> parentGroup.remove(group));

        } else if (item.hasKey("reference")) {
          Action reference = actionManager.getAction(item.getString("reference"));
          if (reference != null) {
            parentGroup.add(reference);
            context.addDisposable(() -> parentGroup.remove(reference));
          }
        }
      }
    }
  }

  private Action handleAction(JsonObject action, String pluginId, PluginContext context) {
    String id = action.getString("id");
    String text = action.getString("text");
    String description = null;
    if (action.hasKey("description")) {
      description = action.getString("description");
    }
    String imageId = null;
    if (action.hasKey("icon")) {
      imageId = action.getString("icon");
    }
    JsAction jsAction = new JsAction(text, description, imageId, imageRegistry);
    actionManager.registerAction(id, jsAction, pluginId);
    context.addDisposable(
        () -> {
          if (actionManager.hasAction(id)) {
            actionManager.unregisterAction(id);
          }
        });
    if (action.hasKey("addToGroup")) {
      JsonValue addToGroup = action.get("addToGroup");
      if (addToGroup.getType() == JsonType.ARRAY) {
        JsonArray groups = (JsonArray) addToGroup;
        for (int i = 0; i < groups.length(); i++) {
          JsonObject object = groups.getObject(i);
          addActionToGroup(jsAction, object, context);
        }
      } else if (addToGroup.getType() == JsonType.OBJECT) {
        addActionToGroup(jsAction, (JsonObject) addToGroup, context);
      }
    }

    if (action.hasKey("keybinding")) {
      JsonObject keybinding = action.getObject("keybinding");
      // TODO add keybinding
    }
    return jsAction;
  }

  private void addActionToGroup(Action action, JsonObject addToGroup, PluginContext context) {
    String groupId = addToGroup.getString("groupId");
    Action actionGroup = actionManager.getAction(groupId);
    if (actionGroup != null) {
      DefaultActionGroup group = (DefaultActionGroup) actionGroup;

      Anchor anchor = null;
      if (addToGroup.hasKey("anchor")) {
        anchor = Anchor.getAnchor(addToGroup.getString("anchor"));
      }

      String relativeId = null;
      if (addToGroup.hasKey("relativeTo")) {
        relativeId = addToGroup.getString("relativeTo");
      }

      group.add(action, new Constraints(anchor, relativeId));
      context.addDisposable(() -> group.remove(action));
    }
  }
}
