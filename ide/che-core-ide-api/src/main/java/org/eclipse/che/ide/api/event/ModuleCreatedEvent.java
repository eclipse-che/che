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
package org.eclipse.che.ide.api.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * @author Vlad Zhukovskiy
 * @author Dmitry Shnurenko
 */
public class ModuleCreatedEvent extends GwtEvent<ModuleCreatedEvent.ModuleCreatedHandler> {
    public interface ModuleCreatedHandler extends EventHandler {
        void onModuleCreated(ModuleCreatedEvent event);
    }

    private static Type<ModuleCreatedHandler> TYPE;

    public static Type<ModuleCreatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private ProjectConfigDto module;

    public ModuleCreatedEvent(ProjectConfigDto module) {
        this.module = module;
    }

    public ProjectConfigDto getModule() {
        return module;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Type<ModuleCreatedHandler> getAssociatedType() {
        return (Type)TYPE;
    }

    @Override
    protected void dispatch(ModuleCreatedHandler handler) {
        handler.onModuleCreated(this);
    }
}
