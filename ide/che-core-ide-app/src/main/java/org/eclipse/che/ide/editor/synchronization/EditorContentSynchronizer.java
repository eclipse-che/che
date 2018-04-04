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
package org.eclipse.che.ide.editor.synchronization;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.resource.Path;

/**
 * The synchronizer of content for opened files with the same {@link Path}. Used to sync the content
 * of opened files in different {@link EditorPartStack}s.
 *
 * @author Roman Nikitenko
 */
public interface EditorContentSynchronizer {
  /**
   * Begins to track given editor to sync its content.
   *
   * @param editor editor to sync content
   */
  void trackEditor(EditorPartPresenter editor);

  /**
   * Stops to track changes of content for given editor.
   *
   * @param editor editor to stop tracking
   */
  void unTrackEditor(EditorPartPresenter editor);
}
