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
package org.eclipse.che.ide.part.editor;

/** Factory for creating instances of {@link AddEditorTabMenu} */
public interface AddEditorTabMenuFactory {

  /**
   * Creates new Add editor tab menu.
   *
   * @return editor tab menu
   */
  AddEditorTabMenu newAddEditorTabMenu();
}
