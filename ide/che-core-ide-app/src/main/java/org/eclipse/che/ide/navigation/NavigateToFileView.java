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
package org.eclipse.che.ide.navigation;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.resource.Path;

/**
 * View for file navigation (find file by name and open it).
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@ImplementedBy(NavigateToFileViewImpl.class)
public interface NavigateToFileView extends View<NavigateToFileView.ActionDelegate> {

  /** Is needed to delegate actions to corresponding presenter. */
  interface ActionDelegate {

    /**
     * Is called when file name is changed.
     *
     * @param fileName file name
     */
    void onFileNameChanged();

    /**
     * Is called when file is selected.
     *
     * @param path file path
     */
    void onFileSelected(Path path);
  }

  /** Show popup. */
  void showPopup();

  /** Hide popup. */
  void hidePopup();

  /**
   * Show suggestion popup with list of items.
   *
   * @param items items of suggestions
   */
  void showItems(List<SearchResultDto> items);

  /**
   * Set the file name input field enabled or disabled.
   *
   * @param enabled {@code true} if field has to be enabled, otherwise {@code false}
   */
  void setFileNameTextBoxEnabled(boolean enabled);

  /** Returns entered file name. */
  String getFileName();
}
