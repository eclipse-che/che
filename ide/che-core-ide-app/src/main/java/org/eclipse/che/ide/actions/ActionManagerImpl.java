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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public class ActionManagerImpl implements ActionManager {
  public static final String[] EMPTY_ARRAY = new String[0];

  private final Map<String, Object> myId2Action;
  private final Map<String, Set<String>> myPlugin2Id;
  private final Map<String, Integer> myId2Index;
  private final Map<Object, String> myAction2Id;
  private final PresentationFactory presentationFactory;
  private final Provider<PerspectiveManager> managerProvider;

  private int myRegisteredActionsCount;

  @Inject
  public ActionManagerImpl(
      PresentationFactory presentationFactory, Provider<PerspectiveManager> managerProvider) {
    this.presentationFactory = presentationFactory;
    this.managerProvider = managerProvider;
    this.myId2Action = new HashMap<>();
    this.myPlugin2Id = new HashMap<>();
    this.myId2Index = new HashMap<>();
    this.myAction2Id = new HashMap<>();
    registerDefaultActionGroups();
  }

  private void registerDefaultActionGroups() {
    // register default action groups for main menu
    DefaultActionGroup mainMenu = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_MAIN_MENU, mainMenu);

    DefaultActionGroup workspaceGroup = new DefaultActionGroup("Workspace", true, this);
    registerAction(IdeActions.GROUP_WORKSPACE, workspaceGroup);
    mainMenu.add(workspaceGroup);

    DefaultActionGroup projectGroup = new DefaultActionGroup("Project", true, this);
    registerAction(IdeActions.GROUP_PROJECT, projectGroup);
    mainMenu.add(projectGroup);

    DefaultActionGroup editGroup = new DefaultActionGroup("Edit", true, this);
    registerAction(IdeActions.GROUP_EDIT, editGroup);
    mainMenu.add(editGroup);

    DefaultActionGroup assistantGroup = new DefaultActionGroup("Assistant", true, this);
    registerAction(IdeActions.GROUP_ASSISTANT, assistantGroup);
    mainMenu.add(assistantGroup);

    DefaultActionGroup runGroup = new DefaultActionGroup("Run", true, this);
    registerAction(IdeActions.GROUP_RUN, runGroup);
    mainMenu.add(runGroup);

    DefaultActionGroup profileGroup = new DefaultActionGroup("Profile", true, this);
    registerAction(IdeActions.GROUP_PROFILE, profileGroup);
    mainMenu.add(profileGroup);

    DefaultActionGroup helpGroup = new DefaultActionGroup("Help", true, this);
    registerAction(IdeActions.GROUP_HELP, helpGroup);
    mainMenu.add(helpGroup);

    // register default action groups for context menu
    DefaultActionGroup mainContextMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_MAIN_CONTEXT_MENU, false, this);
    registerAction(IdeActions.GROUP_MAIN_CONTEXT_MENU, mainContextMenuGroup);

    DefaultActionGroup runContextMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_RUN_CONTEXT_MENU, false, this);
    registerAction(IdeActions.GROUP_RUN_CONTEXT_MENU, runContextMenuGroup);
    mainContextMenuGroup.add(runContextMenuGroup);

    DefaultActionGroup debugContextMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_DEBUG_CONTEXT_MENU, false, this);
    registerAction(IdeActions.GROUP_DEBUG_CONTEXT_MENU, debugContextMenuGroup);
    mainContextMenuGroup.add(debugContextMenuGroup);

    // register default action groups for part menu
    DefaultActionGroup partMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_PART_MENU, false, this);
    registerAction(IdeActions.GROUP_PART_MENU, partMenuGroup);

    // register default action groups for toolbar controller menu
    DefaultActionGroup toolbarControllerMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_TOOLBAR_CONTROLLER, false, this);
    registerAction(IdeActions.GROUP_TOOLBAR_CONTROLLER, toolbarControllerMenuGroup);

    DefaultActionGroup leftMainMenu = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_LEFT_MAIN_MENU, leftMainMenu);

    DefaultActionGroup rightMainMenu = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_RIGHT_MAIN_MENU, rightMainMenu);

    DefaultActionGroup projectExplorerContextMenuGroup =
        new DefaultActionGroup(IdeActions.GROUP_PROJECT_EXPLORER_CONTEXT_MENU, false, this);
    registerAction(IdeActions.GROUP_PROJECT_EXPLORER_CONTEXT_MENU, projectExplorerContextMenuGroup);

    // Register context menu group for editor tab
    registerAction(
        IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU,
        new DefaultActionGroup(IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU, false, this));

    // Register context menu group for consoles tree
    registerAction(
        IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU,
        new DefaultActionGroup(IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU, false, this));

    // register default action groups for main toolbar
    DefaultActionGroup mainToolbarGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_MAIN_TOOLBAR, mainToolbarGroup);
    // register default action groups for center part of toolbar
    DefaultActionGroup centerToolbarGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_CENTER_TOOLBAR, centerToolbarGroup);
    // register default action groups for right part of  toolbar
    DefaultActionGroup rightToolbarGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_RIGHT_TOOLBAR, rightToolbarGroup);

    // register default action groups for status panel
    DefaultActionGroup leftStatusPanelGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_LEFT_STATUS_PANEL, leftStatusPanelGroup);

    DefaultActionGroup centerStatusPanelGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_CENTER_STATUS_PANEL, centerStatusPanelGroup);

    DefaultActionGroup rightStatusPanelGroup = new DefaultActionGroup(this);
    registerAction(IdeActions.GROUP_RIGHT_STATUS_PANEL, rightStatusPanelGroup);
  }

  private static void reportActionError(final String pluginId, final String message) {
    if (pluginId == null) {
      Log.error(ActionManagerImpl.class, message);
    } else {
      Log.error(ActionManagerImpl.class, pluginId, message);
    }
  }

  @Override
  public Action getAction(String id) {
    return getActionImpl(id, false);
  }

  private Action getActionImpl(String id, boolean canReturnStub) {
    return (Action) myId2Action.get(id);
  }

  @Override
  public String getId(Action action) {
    return myAction2Id.get(action);
  }

  @Override
  public String[] getActionIds(String idPrefix) {
    ArrayList<String> idList = new ArrayList<>();
    for (String id : myId2Action.keySet()) {
      if (id.startsWith(idPrefix)) {
        idList.add(id);
      }
    }
    return idList.toArray(new String[idList.size()]);
  }

  @Override
  public boolean isGroup(String actionId) {
    return getActionImpl(actionId, true) instanceof ActionGroup;
  }

  @Override
  public Promise<Void> performActions(
      List<Pair<Action, ActionEvent>> actions, boolean breakOnFail) {
    Promise<Void> promise = Promises.resolve(null);
    return chainActionsRecursively(promise, actions.listIterator(), breakOnFail);
  }

  @Override
  public void performAction(String actionId, Map<String, String> parameters) {
    final Action action;
    if (actionId != null && (action = getAction(actionId)) != null) {
      final Presentation presentation = presentationFactory.getPresentation(action);
      final ActionEvent actionEvent = new ActionEvent(presentation, this, parameters);

      action.update(actionEvent);

      if (presentation.isEnabled() && presentation.isVisible()) {
        action.actionPerformed(actionEvent);
      }
    }
  }

  /**
   * Recursively chains the given promise with the promise that performs the next action from the
   * given iterator.
   */
  private Promise<Void> chainActionsRecursively(
      Promise<Void> promise,
      ListIterator<Pair<Action, ActionEvent>> iterator,
      boolean breakOnFail) {
    if (!iterator.hasNext()) {
      return promise;
    }

    final Pair<Action, ActionEvent> actionWithEvent = iterator.next();

    final Promise<Void> derivedPromise =
        promise.thenPromise(
            new Function<Void, Promise<Void>>() {
              @Override
              public Promise<Void> apply(Void arg) throws FunctionException {
                return promiseAction(actionWithEvent.first, actionWithEvent.second);
              }
            });

    if (breakOnFail) {
      return chainActionsRecursively(derivedPromise, iterator, breakOnFail);
    }

    final Promise<Void> derivedErrorSafePromise =
        derivedPromise.catchErrorPromise(
            new Function<PromiseError, Promise<Void>>() {
              @Override
              public Promise<Void> apply(PromiseError arg) throws FunctionException {
                // 'hide' the error to avoid rejecting chain of promises
                return Promises.resolve(null);
              }
            });

    return chainActionsRecursively(derivedErrorSafePromise, iterator, breakOnFail);
  }

  /**
   * Returns promise returned by PromisableAction or already resolved promise for
   * non-PromisableAction.
   */
  private Promise<Void> promiseAction(final Action action, final ActionEvent event) {
    if (action instanceof PromisableAction) {
      return ((PromisableAction) action).promise(event);
    } else {
      action.actionPerformed(event);
      return Promises.resolve(null);
    }
  }

  public Action getParentGroup(
      final String groupId, final String actionName, final String pluginId) {
    if (groupId == null || groupId.length() == 0) {
      reportActionError(pluginId, actionName + ": attribute \"group-id\" should be defined");
      return null;
    }
    Action parentGroup = getActionImpl(groupId, true);
    if (parentGroup == null) {
      reportActionError(
          pluginId,
          actionName
              + ": group with id \""
              + groupId
              + "\" isn't registered; action will be added to the \"Other\" group");
      parentGroup = getActionImpl(IdeActions.GROUP_OTHER_MENU, true);
    }
    if (!(parentGroup instanceof DefaultActionGroup)) {
      reportActionError(
          pluginId,
          actionName
              + ": group with id \""
              + groupId
              + "\" should be instance of "
              + DefaultActionGroup.class.getName()
              + " but was "
              + parentGroup.getClass());
      return null;
    }
    return parentGroup;
  }

  @Override
  public void registerAction(String actionId, Action action, String pluginId) {
    if (myId2Action.containsKey(actionId)) {
      reportActionError(
          pluginId,
          "action with the ID \""
              + actionId
              + "\" was already registered. Action being registered is "
              + action.toString()
              + "; Registered action is "
              + myId2Action.get(actionId)
              + pluginId);
      return;
    }
    if (myAction2Id.containsKey(action)) {
      reportActionError(
          pluginId,
          "action was already registered for another ID. ID is "
              + myAction2Id.get(action)
              + pluginId);
      return;
    }
    myId2Action.put(actionId, action);
    myId2Index.put(actionId, myRegisteredActionsCount++);
    myAction2Id.put(action, actionId);
    if (pluginId != null && !(action instanceof ActionGroup)) {
      Set<String> pluginActionIds = myPlugin2Id.get(pluginId);
      if (pluginActionIds == null) {
        pluginActionIds = new HashSet<>();
        myPlugin2Id.put(pluginId, pluginActionIds);
      }
      pluginActionIds.add(actionId);
    }
  }

  @Override
  public void registerAction(String actionId, Action action) {
    registerAction(actionId, action, null);
  }

  @Override
  public void unregisterAction(String actionId) {
    if (!myId2Action.containsKey(actionId)) {
      Log.debug(getClass(), "action with ID " + actionId + " wasn't registered");
      return;
    }
    Action oldValue = (Action) myId2Action.remove(actionId);
    myAction2Id.remove(oldValue);
    myId2Index.remove(actionId);

    for (Entry<String, Set<String>> entry : myPlugin2Id.entrySet()) {
      final Set<String> pluginActions = entry.getValue();
      if (pluginActions != null) {
        pluginActions.remove(actionId);
      }
    }
  }

  public Comparator<String> getRegistrationOrderComparator() {
    return new Comparator<String>() {
      public int compare(String id1, String id2) {
        return myId2Index.get(id1) - myId2Index.get(id2);
      }
    };
  }

  public String[] getPluginActions(String pluginName) {
    if (myPlugin2Id.containsKey(pluginName)) {
      final Set<String> pluginActions = myPlugin2Id.get(pluginName);
      return pluginActions.toArray(new String[pluginActions.size()]);
    }
    return EMPTY_ARRAY;
  }

  public Set<String> getActionIds() {
    return new HashSet<>(myId2Action.keySet());
  }
}
