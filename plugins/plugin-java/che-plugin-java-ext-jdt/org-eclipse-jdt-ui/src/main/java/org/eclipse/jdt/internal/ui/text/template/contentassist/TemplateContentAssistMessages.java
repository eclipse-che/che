/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2005 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.template.contentassist;

import org.eclipse.osgi.util.NLS;

/** Helper class to get NLSed messages. */
final class TemplateContentAssistMessages extends NLS {

  private static final String BUNDLE_NAME = TemplateContentAssistMessages.class.getName();

  private TemplateContentAssistMessages() {
    // Do not instantiate
  }

  public static String TemplateProposal_displayString;
  public static String TemplateEvaluator_error_title;

  static {
    NLS.initializeMessages(BUNDLE_NAME, TemplateContentAssistMessages.class);
  }
}
