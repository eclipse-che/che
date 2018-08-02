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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages;

import org.eclipse.che.ide.api.mvp.Presenter;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Interface of java project property page. Describes main methods for all properties pages.
 *
 * @author Valeriy Svydenko
 */
public interface ClasspathPagePresenter extends Presenter {
  /** Needs for delegate updateControls function into PagePresenter. */
  interface DirtyStateListener {
    /** Updates preference view components without content panel. */
    void onDirtyChanged();
  }

  /**
   * Sets new delegate
   *
   * @param delegate
   */
  void setUpdateDelegate(DirtyStateListener delegate);

  /**
   * Return property category. This category will used for grouping elements.
   *
   * @return
   */
  String getCategory();

  /** Returns property page's title. This title will be shown into list of properties. */
  String getTitle();

  /** Returns this property page's icon. This icon will be shown into list of properties. * */
  SVGResource getIcon();

  /**
   * Returns whether this page is changed or not. This information is typically used by the
   * properties presenter to decide when the information is changed.
   *
   * @return <code>true</code> if this page is changed, and <code>false</code> otherwise
   */
  boolean isDirty();

  /** Stores changes. */
  void storeChanges();

  /** Reverts changes. */
  void revertChanges();

  /** Clears all information about previous state. */
  void clearData();

  /**
   * Sets selected node from ws. It's needed for choosing source folder or library.
   *
   * @param path path to node
   */
  void addNode(String path, int kind);

  /** Removes selected node from the property page. */
  void removeNode(String path);
}
