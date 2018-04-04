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
package org.eclipse.che.plugin.pullrequest.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler to be advised when a property of the context object is changed.
 *
 * @author Kevin Pollet
 */
public interface ContextPropertyChangeHandler extends EventHandler {
  /**
   * Called when a property of the context object changed.
   *
   * @param event the {@link ContextPropertyChangeEvent} event.
   */
  void onContextPropertyChange(ContextPropertyChangeEvent event);
}
