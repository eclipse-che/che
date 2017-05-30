/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.data.tree.settings.impl;

import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.data.tree.settings.SettingsProvider;

/**
 * @author Vlad Zhukovskiy
 */
public class DummySettingsProvider implements SettingsProvider {
    @Override
    public NodeSettings getSettings() {
        return NodeSettings.DEFAULT_SETTINGS;
    }

    @Override
    public void setSettings(NodeSettings settings) {
        //stub
    }
}
