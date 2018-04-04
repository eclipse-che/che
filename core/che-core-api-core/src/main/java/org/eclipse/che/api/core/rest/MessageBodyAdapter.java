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
package org.eclipse.che.api.core.rest;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.ws.rs.WebApplicationException;

/**
 * Adapts an entity stream in an implementation specific way.
 *
 * <p>To bind custom adapter:
 *
 * <pre>
 *  Multibinder<MessageBodyAdapter> adaptersBinder = Multibinder.newSetBinder(binder(), MessageBodyAdapter.class);
 *  adaptersBinder.addBinding().to(CustomMessageBodyAdapter.class);
 * </pre>
 *
 * @author Yevhenii Voevodin
 */
@Beta
public interface MessageBodyAdapter {

  /** Returns classes for which adaption will be triggered. */
  Set<Class<?>> getTriggers();

  /**
   * Adapts entity stream to a new one, if necessary.
   *
   * @param entityStream an entity stream
   * @return a new stream with an adapted data or the same {@code entityStream} if there is nothing
   *     to adapt
   */
  InputStream adapt(InputStream entityStream) throws WebApplicationException, IOException;
}
