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
import org.eclipse.che.ide.api.project.node.settings.SettingsProvider;

/**
 * @author Vlad Zhukovskiy
 */
public class JavaNodeSettingsProvider implements SettingsProvider {
    @Override
    public NodeSettings getSettings() {
        return new JavaNodeSettings();
    }

    @Override
    public void setSettings(NodeSettings settings) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
