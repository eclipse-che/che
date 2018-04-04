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
package org.eclipse.che.ide.api.editor;

/**
 * Provider interface for creating new instance of {@link EditorPartPresenter}.
 *
 * @author Evgen Vidolob
 */
public interface EditorProvider {
  /** @return the id of this editor */
  String getId();

  /** @return the description of this editor */
  String getDescription();

  /**
   * Every call this method should return new instance.
   *
   * @return new instance of {@link EditorPartPresenter}
   */
  EditorPartPresenter getEditor();
}
