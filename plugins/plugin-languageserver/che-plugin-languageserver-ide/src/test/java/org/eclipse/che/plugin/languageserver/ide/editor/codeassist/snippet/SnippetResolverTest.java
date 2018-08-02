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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.util.Pair;
import org.junit.Test;

public class SnippetResolverTest {
  private static class TestVarResolver implements VariableResolver {
    private Map<String, String> valueMap = new HashMap<>();

    @SafeVarargs
    public TestVarResolver(Pair<String, String>... values) {
      for (Pair<String, String> pair : values) {
        valueMap.put(pair.first, pair.second);
      }
    }

    @Override
    public boolean isVar(String name) {
      return valueMap.containsKey(name);
    }

    @Override
    public String resolve(String name) {
      return valueMap.get(name);
    }
  }

  @Test
  public void resolveVariables() {
    HasLinkedMode linkedMode = mock(HasLinkedMode.class);
    when(linkedMode.createLinkedModel()).thenReturn(mock(LinkedModel.class));
    when(linkedMode.createLinkedGroup()).thenReturn(mock(LinkedModelGroup.class));
    when(linkedMode.createLinkedModelData()).thenReturn(mock(LinkedModelData.class));
    SnippetResolver snippetResolver =
        new SnippetResolver(
            new TestVarResolver(
                Pair.of("foo", "bar"), Pair.of("lolo", "lala"), Pair.of("bla", null)));
    Pair<String, LinkedModel> resolved = snippetResolver.resolve("${foo}", linkedMode, 25);
    assertEquals("bar", resolved.first);
    resolved = snippetResolver.resolve("${gogo}", linkedMode, 25);
    assertEquals("gogo", resolved.first);
    resolved = snippetResolver.resolve("${foo:dflt}", linkedMode, 25);
    assertEquals("bar", resolved.first);
    resolved = snippetResolver.resolve("${bla:dflt}", linkedMode, 25);
    assertEquals("dflt", resolved.first);
  }
}
