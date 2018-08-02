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
package org.eclipse.che.ide.api.action;

/**
 * Represents a separator.
 *
 * @author Evgen Vidolob
 */
public class Separator extends BaseAction {
  private static final Separator ourInstance = new Separator();

  private String myText;

  public Separator() {
    // It is necessary because otherwise we have some problems with myText==null after compiling GWT
    this(null);
  }

  public Separator(final String text) {
    myText = text;
  }

  public String getText() {
    return myText;
  }

  public static Separator getInstance() {
    return ourInstance;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException();
  }
}
