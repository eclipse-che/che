/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.inject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.requirejs.ModuleHolder;

/**
 * Provider of Orion CodeEdit widget instance.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class OrionCodeEditWidgetProvider implements Provider<OrionCodeEditWidgetOverlay> {

    private final ModuleHolder               moduleHolder;
    private       OrionCodeEditWidgetOverlay orionCodeEditWidgetOverlay;

    @Inject
    public OrionCodeEditWidgetProvider(ModuleHolder moduleHolder) {
        this.moduleHolder = moduleHolder;
    }

    @Override
    public OrionCodeEditWidgetOverlay get() {
        if (orionCodeEditWidgetOverlay == null) {
            OrionCodeEditWidgetOverlay codeEditWidgetModule = moduleHolder.getModule("CodeEditWidget").cast();
            orionCodeEditWidgetOverlay = codeEditWidgetModule.create();
        }
        return orionCodeEditWidgetOverlay;
    }
}
