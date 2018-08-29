/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.search.internal.ui;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The plug-in runtime class for Search plug-in */
public class SearchPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(SearchPlugin.class);

  public static void log(IOException e) {
    LOG.error(e.getMessage(), e);
  }
}
