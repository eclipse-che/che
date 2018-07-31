/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor;

/**
 * Editor auto save functionality. It's supports enable/disable auto save.
 *
 * @author Evgen Vidolob
 */
public interface EditorWithAutoSave {

  /** Return true if auto save is enabled, false otherwise. */
  boolean isAutoSaveEnabled();

  /** Enable auto save. If editor doesn't support auto save do nothing. */
  void enableAutoSave();

  /** Disable auto save. If editor doesn't support auto save do nothing. */
  void disableAutoSave();
}
