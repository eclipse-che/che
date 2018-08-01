/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree.event;

import static org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent.HasBeforeLoadHandlers;

import com.google.gwt.event.shared.HandlerRegistration;
import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent.BeforeLoadHandler;
import org.eclipse.che.ide.ui.smartTree.event.LoadEvent.HasLoadHandlers;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent.HasLoadExceptionHandlers;
import org.eclipse.che.ide.ui.smartTree.event.LoadExceptionEvent.LoadExceptionHandler;

/**
 * Aggregating handler interface for {@link BeforeLoadEvent}, {@link LoadExceptionEvent}, {@link
 * LoadEvent}.
 *
 * @author Vlad Zhukovskiy
 */
public interface LoaderHandler
    extends BeforeLoadHandler,
        LoadExceptionHandler,
        LoadEvent.LoadHandler,
        PostLoadEvent.PostLoadHandler {

  public interface HasLoaderHandlers
      extends HasLoadHandlers,
          HasLoadExceptionHandlers,
          HasBeforeLoadHandlers,
          PostLoadEvent.HasPostLoadHandlers {
    public HandlerRegistration addLoaderHandler(LoaderHandler handler);
  }
}
