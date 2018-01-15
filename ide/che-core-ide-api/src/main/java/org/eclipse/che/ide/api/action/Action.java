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

/**
 * Represents an entity that has a state, a presentation and can be performed.
 *
 * <p>For an action to be useful, you need to implement {@link BaseAction#actionPerformed} and
 * optionally to override {@link BaseAction#update}. By overriding the {@link BaseAction#update}
 * method you can dynamically change action's presentation.
 *
 * <p>The same action can have various presentations.
 *
 * @author Yevhen Vydolob
 */
public interface Action {

  /**
   * Updates the state of the action. Default implementation does nothing. Override this method to
   * provide the ability to dynamically change action's state and(or) presentation depending on the
   * context (For example when your action state depends on the selection you can check for
   * selection and change the state accordingly). This method can be called frequently, for
   * instance, if an action is added to a toolbar, it will be updated twice a second. This means
   * that this method is supposed to work really fast, no real work should be done at this phase.
   * For example, checking selection in a tree or a list, is considered valid, but working with a
   * file system is not. If you cannot understand the state of the action fast you should do it in
   * the {@link #actionPerformed(ActionEvent)} method and notify the user that action cannot be
   * executed if it's the case.
   *
   * @param e Carries information on the invocation place and data available
   */
  void update(ActionEvent e);

  /**
   * Returns a template presentation that will be used as a template for created presentations.
   *
   * @return template presentation
   */
  Presentation getTemplatePresentation();

  /**
   * Implement this method to provide your action handler.
   *
   * @param e Carries information on the invocation place
   */
  void actionPerformed(ActionEvent e);
}
