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
package org.eclipse.che.ide.api.parts;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.eclipse.che.ide.api.resources.VirtualFile;

/** @author Dmitry Shnurenko */
public interface EditorTab extends View<EditorTab.ActionDelegate>, TabItem {

  void setReadOnlyMark(boolean isVisible);

  void setErrorMark(boolean isVisible);

  void setWarningMark(boolean isVisible);

  String getId();

  /**
   * Return virtual file associated with editor tab.
   *
   * @return {@link VirtualFile} file
   */
  VirtualFile getFile();

  /**
   * Sets associated file with editor tab.
   *
   * @param file associated file
   */
  void setFile(VirtualFile file);

  /**
   * Set color to the label of editor's tab.
   *
   * @param color CSS color to set. Supported CSS color values:
   *     <ul>
   *       <li>Hexadecimal colors e.g. #ff0000
   *       <li>RGB colors e.g. rgb(255, 0, 0)
   *       <li>RGBA colors e.g. rgba(255, 0, 0, 0.3)
   *       <li>HSL colors e.g. hsl(120, 60%, 70%)
   *       <li>HSLA colors e.g. hsla(120, 100%, 25%, 0.3)
   *       <li>Predefined/Cross-browser color names e.g. green
   *           <ul/>
   */
  void setTitleColor(String color);

  /**
   * Get editor part which associated with given tab
   *
   * @return editor part which associated with given tab
   */
  EditorPartPresenter getRelativeEditorPart();

  /**
   * Set unsaved data mark to editor tab item.
   *
   * @param hasUnsavedData true if tab should display 'unsaved data' mark, otherwise false
   */
  void setUnsavedDataMark(boolean hasUnsavedData);

  /**
   * Set pin mark to editor tab item.
   *
   * @param pinned true if tab should be pinned, otherwise false
   */
  void setPinMark(boolean pinned);

  /**
   * Indicates whether editor tab is either pinned or not.
   *
   * @return true if editor tab is pinned
   */
  boolean isPinned();

  interface ActionDelegate {

    void onTabClicked(@NotNull TabItem tab);

    void onTabClose(@NotNull TabItem tab);

    void onTabDoubleClicked(@NotNull TabItem tab);
  }
}
