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
