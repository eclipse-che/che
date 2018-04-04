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
package org.eclipse.che.core.db.cascade.event;

import org.eclipse.che.core.db.cascade.CascadeContext;

/**
 * Special event type which is needed only for notification in the process which can require cascade
 * operation.
 *
 * <p>Publisher should invoke {@link #propagateException()} to get cause of event canceling.
 *
 * <p>Rollback of operation must be performed when subscriber throws {@link Exception} during event
 * processing.
 *
 * <p>Usage example:
 *
 * <pre>
 *     EventService bus = new EventService();
 *     bus.subscribe(new CascadeEventSubscriber&lt;MyEvent&gt;() {
 *         &#64;Override
 *         public void onCascadeEvent(MyEvent event) throws Exception {
 *             if (event.getEntityName().startsWith("reserved")) {
 *                 throw new ConflictException("Entity name can't start with `reserved`.");
 *             }
 *         }
 *     });
 *     bus.publish(new MyEvent(...)).propagateException();
 * </pre>
 *
 * @author Anton Korneta
 * @author Sergii Leschenko
 */
public abstract class CascadeEvent {
  protected final CascadeContext context = new CascadeContext();

  public CascadeContext getContext() {
    return context;
  }

  /**
   * Propagates exception if subscriber throws it while event processing otherwise do nothing
   *
   * @throws Exception when any subscriber throws {@link Exception}
   */
  public void propagateException() throws Exception {
    if (context.isFailed()) {
      throw context.getCause();
    }
  }
}
