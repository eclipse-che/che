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
package org.eclipse.che.ide.statepersistance;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMapBinder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.client.WorkspaceStateRestorer;
import org.eclipse.che.ide.editor.EditorAgentImpl;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerStateComponent;
import org.eclipse.che.ide.workspace.WorkspacePresenter;

/**
 * GIN module for configuring Persistence API components.
 *
 * @author Artem Zatsarynnyi
 */
public class PersistenceApiModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMapBinder.newMapBinder(binder(), String.class, WsAgentComponent.class)
                    .addBinding("ZZ Restore Workspace State")
                    .to(WorkspaceStateRestorer.class);

        GinMultibinder<StateComponent> stateComponents = GinMultibinder.newSetBinder(binder(), StateComponent.class);
        stateComponents.addBinding().to(WorkspacePresenter.class);
        stateComponents.addBinding().to(EditorAgentImpl.class);
        stateComponents.addBinding().to(ProjectExplorerStateComponent.class);
    }
}
