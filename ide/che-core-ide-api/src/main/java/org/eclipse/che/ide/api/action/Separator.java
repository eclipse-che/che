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
