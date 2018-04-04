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
