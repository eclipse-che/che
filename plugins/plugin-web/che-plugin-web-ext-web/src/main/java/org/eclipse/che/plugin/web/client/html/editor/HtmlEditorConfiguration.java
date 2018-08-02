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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.editor.changeintercept.ChangeInterceptorProvider;
import org.eclipse.che.ide.api.editor.changeintercept.TextChangeInterceptor;
import org.eclipse.che.ide.api.editor.codeassist.CodeAssistProcessor;
import org.eclipse.che.ide.api.editor.editorconfig.DefaultTextEditorConfiguration;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;

/** The html type editor configuration. */
public class HtmlEditorConfiguration extends DefaultTextEditorConfiguration {

  /** Auto edit factories. */
  private Set<AutoEditStrategyFactory> autoEditStrategyFactories;

  /** Chained processor. */
  private DefaultCodeAssistProcessor defaultProcessor;

  /**
   * Build a new Configuration with the given set of strategies.
   *
   * @param autoEditStrategyFactories the strategy factories
   */
  public HtmlEditorConfiguration(
      Set<AutoEditStrategyFactory> autoEditStrategyFactories,
      DefaultCodeAssistProcessor defaultProcessor) {
    this.autoEditStrategyFactories = autoEditStrategyFactories;
    this.defaultProcessor = defaultProcessor;
  }

  @Override
  public Map<String, CodeAssistProcessor> getContentAssistantProcessors() {
    if (defaultProcessor.getProcessors() == null || defaultProcessor.getProcessors().size() == 0) {
      return null;
    }
    Map<String, CodeAssistProcessor> map = new HashMap<>();
    map.put(DocumentPartitioner.DEFAULT_CONTENT_TYPE, defaultProcessor);
    return map;
  }

  @Override
  public ChangeInterceptorProvider getChangeInterceptorProvider() {
    final ChangeInterceptorProvider parentProvider = super.getChangeInterceptorProvider();
    if (this.autoEditStrategyFactories == null) {
      return parentProvider;
    }
    return new ChangeInterceptorProvider() {
      @Override
      public List<TextChangeInterceptor> getInterceptors(final String contentType) {
        final List<TextChangeInterceptor> result = new ArrayList<>();
        if (parentProvider != null) {
          final List<TextChangeInterceptor> parentProvided =
              parentProvider.getInterceptors(contentType);
          if (parentProvided != null) {
            result.addAll(parentProvided);
          }
        }

        for (AutoEditStrategyFactory strategyFactory : autoEditStrategyFactories) {
          final TextChangeInterceptor interceptor = strategyFactory.build(contentType);
          result.add(interceptor);
        }
        return result;
      }
    };
  }
}
