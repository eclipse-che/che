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
package org.eclipse.che.plugin.web.client.html.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.eclipse.che.ide.api.editor.codeassist.DefaultChainedCodeAssistProcessor;

/**
 * Allows to chain code assist processor for the default given content type. It will delegate to sub
 * processors.
 *
 * @author Florent Benoit
 */
@Singleton
public class DefaultCodeAssistProcessor extends DefaultChainedCodeAssistProcessor {

  /** HTML code assist processors.(as it's optional it can't be in constructor) */
  @Inject(optional = true)
  protected void injectProcessors(Set<HTMLCodeAssistProcessor> htmlCodeAssistProcessors) {
    setProcessors(htmlCodeAssistProcessors);
  }
}
