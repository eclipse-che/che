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
import java.util.Set;
import javax.inject.Provider;

/**
 * Guice Provider for HTML Editor configuration.
 *
 * @author Florent Benoit
 */
public class HTMLEditorConfigurationProvider implements Provider<HtmlEditorConfiguration> {

  /** Auto Edit strategies with HTML editor scope */
  @Inject(optional = true)
  private Set<AutoEditStrategyFactory> autoEditStrategyFactories;

  @Inject private DefaultCodeAssistProcessor chainedCodeAssistProcessor;

  /**
   * Build a new instance of HtmlEditor Configuration
   *
   * @return
   */
  @Override
  public HtmlEditorConfiguration get() {
    return new HtmlEditorConfiguration(autoEditStrategyFactories, chainedCodeAssistProcessor);
  }
}
