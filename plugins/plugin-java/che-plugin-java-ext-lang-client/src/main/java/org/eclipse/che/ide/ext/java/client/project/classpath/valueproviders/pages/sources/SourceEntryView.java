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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

import com.google.inject.ImplementedBy;
import java.util.Map;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * View interface for the information about source folders on the build path.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(SourceEntryViewImpl.class)
public interface SourceEntryView extends View<SourceEntryView.ActionDelegate> {
  /** Clear and Render sources. */
  void renderNodes();

  /**
   * Set sources map for displaying.
   *
   * @param data map which binds categories of the sources
   */
  void setData(Map<String, ClasspathEntry> data);

  /**
   * Removes node from the sources.
   *
   * @param nodeWidget widget which should be removed
   */
  void removeNode(NodeWidget nodeWidget);

  /** Sets enabled state of the 'Add Source' button. */
  void setAddSourceButtonState(boolean enabled);

  /** Clears sources panel. */
  void clear();

  interface ActionDelegate {
    /** Returns true if project is plain. */
    boolean isPlainJava();

    /** Performs some actions when user click on Add Jar button. */
    void onAddSourceClicked();

    /** Performs some actions when user click on Remove button. */
    void onRemoveClicked(String path);
  }
}
