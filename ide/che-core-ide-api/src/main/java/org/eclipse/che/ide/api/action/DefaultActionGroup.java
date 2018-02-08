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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.util.loging.Log;

/**
 * A default implementation of {@link ActionGroup}. Provides the ability to add children actions and
 * separators between them. In most of the cases you will be using this implementation but note that
 * there are cases where children are determined on rules different than just positional
 * constraints, that's when you need to implement your own <code>ActionGroup</code>.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class DefaultActionGroup extends ActionGroup {
  private static final Action[] EMPTY_ACTIONS = new Action[0];

  /** Contains actions */
  private final List<Action> actionList = new ArrayList<>();

  /** Contains constraints */
  private final List<Constraints> constraintsList = new ArrayList<>();

  /** Contains instances of sorted Actions */
  private Action[] sortedActions;

  private ActionManager actionManager;

  // TODO: think about removing this field
  private boolean needSorting = false;

  public DefaultActionGroup(ActionManager actionManager) {
    this(null, false, actionManager);
  }

  /**
   * Creates an action group containing the specified actions.
   *
   * @param actions the actions to add to the group
   */
  public DefaultActionGroup(ActionManager actionManager, Action... actions) {
    this(null, false, actionManager);
    for (Action action : actions) {
      add(action);
    }
  }

  public DefaultActionGroup(String shortName, boolean popup, ActionManager actionManager) {
    super(shortName, popup);
    this.actionManager = actionManager;
  }

  private static int findIndex(String actionId, List<Action> actions, ActionManager actionManager) {
    for (int i = 0; i < actions.size(); i++) {
      Action action = actions.get(i);
      if (action != null) {
        String id = actionManager.getId(action);
        if (id != null && id.equals(actionId)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Adds the specified action to the tail.
   *
   * @param action Action to be added
   * @param actionManager ActionManager instance
   */
  public final void add(Action action, ActionManager actionManager) {
    add(action, Constraints.LAST, actionManager);
  }

  public final void add(Action action) {
    addAction(action, Constraints.LAST);
  }

  public final void addAction(Action action) {
    addAction(action, Constraints.LAST);
  }

  /** Adds a separator to the tail. */
  public final void addSeparator() {
    add(Separator.getInstance());
  }

  public final void add(Action action, Constraints constraint) {
    add(action, constraint, actionManager);
  }

  public final void addAction(Action action, Constraints constraint) {
    addAction(action, constraint, actionManager);
  }

  /**
   * Adds the specified action with the specified constraint.
   *
   * @param action Action to be added; cannot be null
   * @param constraint Constraint to be used for determining action's position; cannot be null
   * @throws IllegalArgumentException in case when:
   *     <li>action is null
   *     <li>constraint is null
   *     <li>action is already in the group
   */
  public final void add(Action action, Constraints constraint, ActionManager actionManager) {
    addAction(action, constraint, actionManager);
  }

  public final void addAction(Action action, Constraints constraint, ActionManager actionManager) {
    if (action == this) {
      throw new IllegalArgumentException("Cannot add a group to itself");
    }

    // Check that action isn't already registered
    if (!(action instanceof Separator)) {
      for (Action actionInList : actionList) {
        if (action.equals(actionInList)) {
          Log.error(getClass(), "Can not add an action twice: " + action);
          return;
        }
      }
    }
    actionList.add(action);
    constraintsList.add(constraint);

    needSorting = true;
  }

  /**
   * Removes specified action from group.
   *
   * @param actionToRemove Action to be removed
   */
  public final void remove(Action actionToRemove) {
    int index;
    for (Action action : actionList) {
      if (action.equals(actionToRemove)) {
        index = actionList.indexOf(action);

        actionList.remove(action);
        constraintsList.remove(index);

        needSorting = true;
        return;
      }
    }
  }

  /** Removes all children actions (separators as well) from the group. */
  public final void removeAll() {
    actionList.clear();
    constraintsList.clear();
    needSorting = true;
  }

  /**
   * Returns group's children in the order determined by constraints.
   *
   * @param e not used
   * @return An array of children actions
   */
  @Override
  public final Action[] getChildren(ActionEvent e) {

    if (needSorting) {
      sortedActions = getSortedActions();
      needSorting = false;
    }
    return sortedActions == null ? EMPTY_ACTIONS : sortedActions;
  }

  /**
   * Sorts actions depending on their constraints and their input order.
   *
   * @return An array of sorted actions
   */
  // TODO: to complicate
  private Action[] getSortedActions() {
    List<Action> result = new ArrayList<>();
    Map<Action, Constraints> unsortedMap = new LinkedHashMap<>();

    for (int i = 0; i < actionList.size(); i++) {

      Action action = actionList.get(i);
      Constraints constraints = constraintsList.get(i);

      // if action is added to result list, it needs to call
      // checkUnsorted method to look for another actions, that must be
      // before or after this action
      if (constraints.myAnchor.equals(Anchor.FIRST)) {
        result.add(0, action);
        checkUnsorted(unsortedMap, action, result);
      } else if (constraints.myAnchor.equals(Anchor.LAST)) {
        result.add(action);
        checkUnsorted(unsortedMap, action, result);
      } else {
        // find related action in result list, if found, add action
        // before or after it. If not, add to unsorted map
        int index = findIndex(constraints.relativeId, result, actionManager);
        if (index == -1) {
          unsortedMap.put(action, constraints);
        } else {
          if (constraints.myAnchor.equals(Anchor.BEFORE)) {
            result.add(index, action);
            checkUnsorted(unsortedMap, action, result);
          } else if (constraints.myAnchor.equals(Anchor.AFTER)) {
            result.add(index + 1, action);
            checkUnsorted(unsortedMap, action, result);
          }
        }
      }
    }
    // append left unsorted actions to the end
    result.addAll(unsortedMap.keySet());
    return result.toArray(new Action[result.size()]);
  }

  /**
   * This method checks unsorted map for actions, that depend on action, received in parameter. If
   * found ones, adds it
   *
   * @param unsortedMap - map with unsorted actions
   * @param action - action, that is a condition for actions in unsorted list
   * @param result - result list
   */
  private void checkUnsorted(
      Map<Action, Constraints> unsortedMap, Action action, List<Action> result) {
    Iterator<Map.Entry<Action, Constraints>> itr = unsortedMap.entrySet().iterator();
    while (itr.hasNext()) {
      Map.Entry<Action, Constraints> entry = itr.next();

      String actionId = actionManager.getId(action);

      Action relatedAction = entry.getKey();
      Constraints relatedConstraints = entry.getValue();

      // if dependant action constraints match depends on our action
      // add it to result and remove from unsorted list
      if (relatedConstraints.relativeId.equals(actionId)) {
        if (relatedConstraints.myAnchor.equals(Anchor.BEFORE)) {
          result.add(result.indexOf(action), relatedAction);
        } else if (relatedConstraints.myAnchor.equals(Anchor.AFTER)) {
          result.add(result.indexOf(action) + 1, relatedAction);
        }
        itr.remove();
        // recursive call of this method, but now passing the 'relatedAction'
        // to find another actions, that related to 'relatedAction
        checkUnsorted(unsortedMap, relatedAction, result);
      }
    }
  }

  /**
   * Returns the number of contained children (including separators).
   *
   * @return number of children in the group
   */
  public final int getChildrenCount() {
    return actionList.size();
  }

  public final Action[] getChildActionsOrStubs() {
    return sortedActions == null ? EMPTY_ACTIONS : sortedActions;
  }

  public final void addAll(ActionGroup group) {
    for (Action each : group.getChildren(null)) {
      add(each);
    }
  }

  public final void addAll(Collection<Action> actionList) {
    for (Action each : actionList) {
      add(each);
    }
  }

  public final void addAll(Action... actions) {
    for (Action each : actions) {
      add(each);
    }
  }

  public void addSeparator(String separatorText) {
    add(new Separator(separatorText));
  }
}
