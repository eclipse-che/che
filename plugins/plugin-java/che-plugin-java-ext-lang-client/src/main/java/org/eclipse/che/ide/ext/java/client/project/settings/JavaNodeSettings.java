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
package org.eclipse.che.ide.ext.java.client.project.settings;

import org.eclipse.che.ide.api.project.node.settings.NodeSettings;

/**
 * @author Vlad Zhukovskiy
 */
public class JavaNodeSettings implements NodeSettings {

    private boolean showEmptyPackages = false;

    private boolean showExternalLibrariesNode = true;

    private boolean showHiddenFiles;

    @Override
    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    @Override
    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
    }

    @Override
    public boolean isFoldersAlwaysOnTop() {
        return false; //TODO make it configurable
    }

    public boolean isShowEmptyPackages() {
        return showEmptyPackages;
    }

    public void setShowEmptyPackages(boolean showEmptyPackages) {
        this.showEmptyPackages = showEmptyPackages;
    }

    public boolean isShowExternalLibrariesNode() {
        return showExternalLibrariesNode;
    }

    public void setShowExternalLibrariesNode(boolean showExternalLibrariesNode) {
        this.showExternalLibrariesNode = showExternalLibrariesNode;
    }
}
