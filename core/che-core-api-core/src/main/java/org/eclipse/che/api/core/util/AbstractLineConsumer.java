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
package org.eclipse.che.api.core.util;

import java.io.IOException;

/**
 * No-op implementation of {@link LineConsumer}
 *
 * @author Alexander Garagatyi
 */
public abstract class AbstractLineConsumer implements LineConsumer {
  @Override
  public void writeLine(String line) throws IOException {}

  @Override
  public void close() throws IOException {}
}
