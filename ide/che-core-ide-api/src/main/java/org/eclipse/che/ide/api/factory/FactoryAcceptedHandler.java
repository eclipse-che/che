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
package org.eclipse.che.ide.api.factory;

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler for FactoryAcceptedEvent. You can use this handler in case need to do some action on
 * after accepting factory
 *
 * @author Vitalii Parfonov
 */
public interface FactoryAcceptedHandler extends EventHandler {

  /**
   * Will be called the factory accepted on IDE side. Project already imported, actions performed.
   *
   * @param event
   */
  void onFactoryAccepted(FactoryAcceptedEvent event);
}
