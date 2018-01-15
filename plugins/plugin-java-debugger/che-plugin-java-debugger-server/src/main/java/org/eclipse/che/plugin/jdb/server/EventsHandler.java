/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server;

import com.sun.jdi.event.EventSet;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Handle events from {@link EventsCollector}.
 *
 * @author andrew00x
 * @see EventsCollector#run()
 */
public interface EventsHandler {
  void handleEvents(EventSet events) throws DebuggerException;
}
