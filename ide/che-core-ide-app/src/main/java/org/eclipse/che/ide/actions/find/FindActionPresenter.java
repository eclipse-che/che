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
package org.eclipse.che.ide.actions.find;

import static java.util.Collections.unmodifiableList;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.actions.ActionManagerImpl;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.UnicodeUtils;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FindActionPresenter implements Presenter, FindActionView.ActionDelegate {

  private final PresentationFactory presentationFactory;
  private final FindActionView view;
  private final ActionManager actionManager;
  private final Map<Action, String> actionsMap;
  private final Comparator<Action> actionComparator =
      new Comparator<Action>() {
        @Override
        public int compare(Action o1, Action o2) {
          int compare =
              compare(
                  o1.getTemplatePresentation().getText(), o2.getTemplatePresentation().getText());
          if (compare == 0 && !o1.equals(o2)) {
            return o1.hashCode() - o2.hashCode();
          }
          return compare;
        }

        public int compare(@Nullable String o1, @Nullable String o2) {
          if (o1 == null) return o2 == null ? 0 : -1;
          if (o2 == null) return 1;
          return o1.compareTo(o2);
        }
      };

  @Inject
  public FindActionPresenter(FindActionView view, ActionManager actionManager) {
    this.view = view;
    this.actionManager = actionManager;
    view.setDelegate(this);
    presentationFactory = new PresentationFactory();
    actionsMap = new TreeMap<>(actionComparator);
  }

  private static boolean containsOnlyUppercaseLetters(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c != '*' && c != ' ' && !Character.isUpperCase(c)) return false;
    }
    return true;
  }

  @Override
  public void go(AcceptsOneWidget container) {}

  public void show() {
    view.show();
    Action action = actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
    collectActions(actionsMap, (ActionGroup) action, action.getTemplatePresentation().getText());
    view.focusOnInput();
    if (view.getName() != null) {
      nameChanged(view.getName(), view.getCheckBoxState());
    }
  }

  private void collectActions(
      Map<Action, String> result, ActionGroup group, final String containingGroupName) {
    final Action[] actions = group.getChildren(null);
    includeGroup(result, group, actions, containingGroupName);
    for (Action action : actions) {
      if (action != null) {
        if (action instanceof ActionGroup) {
          final ActionGroup actionGroup = (ActionGroup) action;
          final String groupName = actionGroup.getTemplatePresentation().getText();
          collectActions(
              result,
              actionGroup,
              StringUtils.isNullOrEmpty(groupName) || !actionGroup.isPopup()
                  ? containingGroupName
                  : groupName);
        } else {
          final String groupName = group.getTemplatePresentation().getText();
          if (result.containsKey(action)) {
            result.put(action, null);
          } else {
            result.put(
                action, StringUtils.isNullOrEmpty(groupName) ? containingGroupName : groupName);
          }
        }
      }
    }
  }

  private void includeGroup(
      Map<Action, String> result, ActionGroup group, Action[] actions, String containingGroupName) {
    boolean showGroup = true;
    for (Action action : actions) {
      if (actionManager.getId(action) != null) {
        showGroup = false;
        break;
      }
    }
    if (showGroup) {
      result.put(group, containingGroupName);
    }
  }

  @Override
  public void nameChanged(String name, boolean checkBoxState) {
    if (name.isEmpty()) {
      view.hideActions();
      return;
    }
    String pattern = convertPattern(name.trim());
    RegExp regExp = RegExp.compile(pattern);
    Map<Action, String> actions = new TreeMap<>(actionComparator);
    if (checkBoxState) {
      Set<String> ids = ((ActionManagerImpl) actionManager).getActionIds();
      for (Action action : actionsMap.keySet()) {
        ids.remove(actionManager.getId(action));
      }
      for (String id : ids) {
        Action action = actionManager.getAction(id);
        Presentation presentation = action.getTemplatePresentation();
        String text = presentation.getText();
        if (text != null && regExp.test(text)) {
          actions.put(action, null);
        }
      }
    }

    List<String> excludedActionIds = getExcludedActionIds(actionManager);

    for (Entry<Action, String> entry : actionsMap.entrySet()) {
      final Action action = entry.getKey();
      final String groupName = entry.getValue();

      if (excludedActionIds.contains(actionManager.getId(action))) {
        continue;
      }

      Presentation presentation = action.getTemplatePresentation();
      String text = presentation.getText();
      if (text != null && regExp.test(text)) {
        actions.put(action, groupName);
      }
    }

    if (!actions.isEmpty()) {
      view.showActions(actions);
    } else {
      view.hideActions();
    }
  }

  @Override
  public void onClose() {
    actionsMap.clear();
  }

  @Override
  public void onActionSelected(Action action) {
    ActionEvent e = new ActionEvent(presentationFactory.getPresentation(action), actionManager);
    action.update(e);
    if (e.getPresentation().isEnabled() && e.getPresentation().isVisible()) {
      view.hide();
      action.actionPerformed(e);
    }
  }

  private String convertPattern(String pattern) {
    final int eol = pattern.indexOf('\n');
    if (eol != -1) {
      pattern = pattern.substring(0, eol);
    }
    if (pattern.length() >= 80) {
      pattern = pattern.substring(0, 80);
    }

    final StringBuilder buffer = new StringBuilder();

    boolean allowToLower = true;
    if (containsOnlyUppercaseLetters(pattern)) {
      allowToLower = false;
    }

    boolean firstIdentifierLetter = true;
    for (int i = 0; i < pattern.length(); i++) {
      final char c = pattern.charAt(i);
      if (Character.isLetterOrDigit(c)
          || UnicodeUtils.regexpIdentifierOrWhitespace.test(String.valueOf(c))) {
        // This logic allows to use uppercase letters only to catch the name like PDM for
        // PsiDocumentManager
        if (Character.isUpperCase(c) || Character.isDigit(c)) {

          if (!firstIdentifierLetter) {
            buffer.append("[^A-Z]*");
          }

          buffer.append("[");
          buffer.append(c);
          if (allowToLower || i == 0) {
            buffer.append('|');
            buffer.append(Character.toLowerCase(c));
          }
          buffer.append("]");
        } else if (Character.isLowerCase(c)) {
          buffer.append('[');
          buffer.append(c);
          buffer.append('|');
          buffer.append(Character.toUpperCase(c));
          buffer.append(']');
        } else {
          buffer.append(c);
        }

        firstIdentifierLetter = false;
      } else if (c == '*') {
        buffer.append(".*");
        firstIdentifierLetter = true;
      } else if (c == '.') {
        buffer.append("\\.");
        firstIdentifierLetter = true;
      } else if (c == ' ') {
        buffer.append("[^A-Z]*\\ ");
        firstIdentifierLetter = true;
      } else {
        firstIdentifierLetter = true;
        // for standard RegExp engine
        buffer.append("\\u");
        buffer.append(Integer.toHexString(c + 0x20000).substring(1));

        // for OROMATCHER RegExp engine
        //                buffer.append("\\x");
        //                buffer.append(Integer.toHexString(c + 0x20000).substring(3));
      }
    }

    return buffer.toString();
  }

  private List<String> getExcludedActionIds(ActionManager actionManager) {
    List<String> ids = new ArrayList<>();

    DefaultActionGroup editGroup =
        (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_RECENT_FILES);
    Action[] children = editGroup.getChildActionsOrStubs();
    for (Action child : children) {
      if (child instanceof Separator) {
        continue;
      }

      ids.add(actionManager.getId(child));
    }

    return unmodifiableList(ids);
  }
}
