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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.HandlerRegistration;

import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent.BeforeLoadHandler;
import org.eclipse.che.ide.ui.smartTree.event.LoadEvent.HasLoadHandlers;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent.HasLoadExceptionHandlers;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent.LoadExceptionHandler;

import static org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent.HasBeforeLoadHandlers;

/**
 * Aggregating handler interface for {@link BeforeLoadEvent}, {@link LoadExceptionEvent}, {@link LoadEvent}.
 *
 * @author Vlad Zhukovskiy
 */
public interface LoaderHandler extends BeforeLoadHandler, LoadExceptionHandler, LoadEvent.LoadHandler, PostLoadEvent.PostLoadHandler {

    public interface HasLoaderHandlers extends HasLoadHandlers, HasLoadExceptionHandlers, HasBeforeLoadHandlers,
                                               PostLoadEvent.HasPostLoadHandlers {
        public HandlerRegistration addLoaderHandler(LoaderHandler handler);
    }
}
