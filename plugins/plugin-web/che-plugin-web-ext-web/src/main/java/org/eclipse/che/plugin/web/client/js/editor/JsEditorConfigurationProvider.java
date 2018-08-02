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
package org.eclipse.che.plugin.web.client.js.editor;

import com.google.inject.Inject;
import java.util.Set;
import javax.inject.Provider;
import org.eclipse.che.plugin.web.client.html.editor.AutoEditStrategyFactory;

/**
 * Provider for JS Editor configuration.
 *
 * @author Florent Benoit
 */
public class JsEditorConfigurationProvider implements Provider<JsEditorConfiguration> {

  /** Auto Edit strategies */
  @Inject(optional = true)
  private Set<AutoEditStrategyFactory> autoEditStrategyFactories;

  @Inject private DefaultCodeAssistProcessor chainedCodeAssistProcessor;

  /**
   * Build a new instance of JS Editor configuration
   *
   * @return
   */
  @Override
  public JsEditorConfiguration get() {
    return new JsEditorConfiguration(autoEditStrategyFactories, chainedCodeAssistProcessor);
  }
}
