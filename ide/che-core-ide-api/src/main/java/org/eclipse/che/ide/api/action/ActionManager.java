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
package org.eclipse.che.ide.api.action;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.extension.SDK;
import org.eclipse.che.ide.util.Pair;

/**
 * A manager for actions. Used to register and unregister actions, also contains utility methods to
 * easily fetch action by id and id by action.
 *
 * @author Evgen Vidolob
 * @see BaseAction
 */
@SDK(title = "ide.api.ui.action")
public interface ActionManager {

  /**
   * Returns action associated with the specified actionId.
   *
   * @param actionId Id of the registered action
   * @return Action associated with the specified actionId, <code>null</code> if there is no actions
   *     associated with the specified actionId
   * @throws java.lang.IllegalArgumentException if <code>actionId</code> is <code>null</code>
   */
  Action getAction(String actionId);

  /**
   * Returns actionId associated with the specified action.
   *
   * @return id associated with the specified action, <code>null</code> if action is not registered
   * @throws java.lang.IllegalArgumentException if <code>action</code> is <code>null</code>
   */
  String getId(Action action);

  /**
   * Registers the specified action with the specified id. Note that IDE keymaps processing deals
   * only with registered actions.
   *
   * @param actionId Id to associate with the action
   * @param action Action to register
   */
  void registerAction(String actionId, Action action);

  /**
   * Registers the specified action with the specified id.
   *
   * @param actionId Id to associate with the action
   * @param action Action to register
   * @param extensionId Identifier of the extension owning the action. Used to show the actions in
   *     the correct place under the "Plugins" node in the "Keymap" settings pane and similar
   *     dialogs.
   */
  void registerAction(String actionId, Action action, String extensionId);

  /**
   * Unregisters the action with the specified actionId.
   *
   * @param actionId Id of the action to be unregistered
   */
  void unregisterAction(String actionId);

  /**
   * Returns the list of all registered action IDs with the specified prefix.
   *
   * @return all action <code>id</code>s which have the specified prefix.
   */
  String[] getActionIds(String idPrefix);

  /**
   * Checks if the specified action ID represents an action group and not an individual action.
   * Calling this method does not cause instantiation of a specific action class corresponding to
   * the action ID.
   *
   * @param actionId the ID to check.
   * @return true if the ID represents an action group, false otherwise.
   */
  boolean isGroup(String actionId);

  /**
   * Performs the given actions and returns {@link Promise}. The purpose of the returned {@link
   * Promise} is to allow for interested parties to be notified when performing given {@code
   * actions} has completed or rejected.
   *
   * @param actions sequence of actions to perform. May contains {@link PromisableAction}.
   * @param breakOnFail if {@code true} - returned promise will be rejected when first {@link
   *     PromisableAction} (from the given {@code actions}) is rejected;<br>
   *     if {@code false} - all the given actions will try to perform in despite of rejecting any
   *     {@link PromisableAction}
   * @return {@link Promise} that will be fulfilled immediately after all the given {@code actions}
   *     are performed. If any {@link PromisableAction} in the given {@code actions} is rejected the
   *     returned promise is rejected as well.
   * @see PromisableAction
   */
  Promise<Void> performActions(List<Pair<Action, ActionEvent>> actions, boolean breakOnFail);

  /**
   * Performs registered action by given id.
   *
   * <p>Note that if either action is not registered or actionId is null then nothing will be done
   *
   * @param actionId the id of action that will be performed
   * @param parameters the parameters which are used for the action invocation
   */
  void performAction(String actionId, Map<String, String> parameters);
}
