/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory for writable output.
 *
 * @author andrew00x
 */
public interface OutputProvider {
  /**
   * Get writable output.
   *
   * @return writable output
   * @throws IOException if an i/o error occurs
   */
  OutputStream getOutputStream() throws IOException;
}
