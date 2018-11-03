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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.libraries;

import com.google.inject.ImplementedBy;
import java.util.Map;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * View interface for the information about JARs and class folders on the build path.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(LibEntryViewImpl.class)
public interface LibEntryView extends View<LibEntryView.ActionDelegate> {
  /** Clear and Render libraries. */
  void renderLibraries();

  /**
   * Set libraries map for displaying.
   *
   * @param data map which binds categories of the library
   */
  void setData(Map<String, ClasspathEntry> data);

  /** Sets enabled state of the 'Add Jar' button. */
  void setAddJarButtonState(boolean enabled);

  interface ActionDelegate {
    /** Returns true if project is plain java. */
    boolean isPlainJava();

    /** Performs some actions when user click on Add Jar button. */
    void onAddJarClicked();

    /** Performs some actions when user click on Remove button. */
    void onRemoveClicked(String path);
  }
}
