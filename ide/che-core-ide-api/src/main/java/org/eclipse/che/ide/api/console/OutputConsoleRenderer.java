/*
 * Copyright (c) 2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.console;

/**
 * An interface to allow output text customizations
 *
 * @author Victor Rubezhny
 */
public interface OutputConsoleRenderer {
  /** Checks if the specified text can be/has to be rendered */
  boolean canRender(String text);

  /** Returns the result of rendering for the specified text */
  String render(String text);

  /** Returns the renderer name */
  String getName();
}
