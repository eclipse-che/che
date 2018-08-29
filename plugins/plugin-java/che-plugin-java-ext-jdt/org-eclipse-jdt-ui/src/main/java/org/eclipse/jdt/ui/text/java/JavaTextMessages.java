/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.text.java;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.1
 */
final class JavaTextMessages extends NLS {

  private static final String BUNDLE_NAME = JavaTextMessages.class.getName();

  private JavaTextMessages() {
    // Do not instantiate
  }

  public static String ResultCollector_anonymous_type;
  public static String ResultCollector_overridingmethod;

  static {
    NLS.initializeMessages(BUNDLE_NAME, JavaTextMessages.class);
  }
}
