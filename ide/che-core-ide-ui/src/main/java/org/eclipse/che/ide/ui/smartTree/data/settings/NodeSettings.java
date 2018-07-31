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
package org.eclipse.che.ide.ui.smartTree.data.settings;

/**
 * Common view settings for tree.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeSettings {
  boolean isShowHiddenFiles();

  boolean isFoldersAlwaysOnTop();

  void setShowHiddenFiles(boolean show);

  NodeSettings DEFAULT_SETTINGS =
      new NodeSettings() {
        boolean showHiddenFiles;

        @Override
        public boolean isShowHiddenFiles() {
          return showHiddenFiles;
        }

        @Override
        public void setShowHiddenFiles(boolean show) {
          this.showHiddenFiles = show;
        }

        @Override
        public boolean isFoldersAlwaysOnTop() {
          return false;
        }
      };
}
