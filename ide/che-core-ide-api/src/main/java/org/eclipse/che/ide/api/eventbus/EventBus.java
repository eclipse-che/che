/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.api.eventbus;

import jsinterop.annotations.JsType;
import org.eclipse.che.ide.api.Disposable;

/** @author Yevhen Vydolob */
@JsType
public interface EventBus {

  /**
   * Fire a event.
   *
   * @param eventType the event type
   * @param event the event
   * @return a reference to this
   */
  <E> EventBus fire(EventType<E> eventType, E event);

  <E> Disposable addHandler(EventType<E> eventType, Handler<E> handler);
}
