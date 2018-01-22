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
package org.eclipse.che.ide.console.linkifiers;

import org.eclipse.che.ide.api.outputconsole.OutputConsole;

/**
 * OutputLinkifier uses for creation clickable elements
 * in the {@link OutputConsole}.
 * OutputLinkifier contains information about creation clickable link
 * from some part text from {@link OutputConsole}
 *
 * @author Oleksandr Andriienko
 * */
public interface OutputLinkifier {

  /**
   * Returns regexp to find some text, which should be rendered like clickable link.
   */
  String getRegExpr();

  /**
   * Does some actions on link click
   * @param lineContent - whole content {@link OutputConsole} line where is located link.
   */
  void onClickLink(String lineContent);

  /**
   * Number regexp group which should be rendered like clickable link.
   * For example we want to transformate text by regexp ".*(Hello)\\s(World).*".
   * If we want render clickable link on the group "Hello" than matchIndex = 1,
   * because "Hello" it's a first group.
   * If we want render clickable link on the group "World" than matchIndex = 2,
   * because "World" is a second group.
   */
  int getMatchIndex();
}
