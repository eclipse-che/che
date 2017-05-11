/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.logger.logging.impl;

import com.google.gwt.core.client.GWT;
import org.slf4j.ILoggerFactory;

/**
 * *
 */
public class Impl {
  public static final ILoggerFactory LOGGER_FACTORY = GWT.create(ILoggerFactory.class);

  private Impl() {
  }
}
