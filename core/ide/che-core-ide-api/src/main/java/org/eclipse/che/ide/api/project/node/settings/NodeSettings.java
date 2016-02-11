/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.project.node.settings;

/**
 * Common view settings for tree.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeSettings {
    boolean isShowHiddenFiles();

    boolean isFoldersAlwaysOnTop();

    void setShowHiddenFiles(boolean show);

    NodeSettings DEFAULT_SETTINGS = new NodeSettings() {
        boolean showHiddenFiles;

        @Override
        public boolean isShowHiddenFiles() {
            return showHiddenFiles;
        }

        public void setShowHiddenFiles(boolean show) {
            this.showHiddenFiles = show;
        }

        @Override
        public boolean isFoldersAlwaysOnTop() {
            return false;
        }
    };
}
