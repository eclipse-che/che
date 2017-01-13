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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebugger;
import org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebuggerResources;

/**
 * NodeJs debug configuration type.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class NodeJsDebuggerConfigurationType implements DebugConfigurationType {

    public static final String DISPLAY_NAME = "NodeJs";

    private final NodeJsDebuggerConfigurationPagePresenter page;

    @Inject
    public NodeJsDebuggerConfigurationType(NodeJsDebuggerConfigurationPagePresenter page,
                                           IconRegistry iconRegistry,
                                           NodeJsDebuggerResources resources) {
        this.page = page;
        iconRegistry.registerIcon(new Icon(NodeJsDebugger.ID + ".debug.configuration.type.icon", resources.nodeJsDebugConfigurationType()));
    }

    @Override
    public String getId() {
        return NodeJsDebugger.ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
        return page;
    }
}
