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
package org.eclipse.che.api.core.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Consumes text line by line for analysing, writing, storing, etc.
 *
 * @author andrew00x
 * @see AbstractLineConsumer
 */
public interface LineConsumer extends Closeable {
  /** Consumes single line. */
  void writeLine(String line) throws IOException;

  LineConsumer DEV_NULL = new AbstractLineConsumer() {};
}
