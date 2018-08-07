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
package org.eclipse.che.api.languageserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.languageserver.shared.model.SnippetParameters;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SnippetTest {
  private FsManager fsManager;
  private TextDocumentService service;

  @BeforeMethod
  public void setUp() throws NotFoundException, ConflictException, ServerException {
    fsManager = mock(FsManager.class);
    when(fsManager.existsAsFile(any())).thenReturn(true);
    when(fsManager.read(anyString()))
        .thenReturn(getClass().getResourceAsStream("/snippettest/File1.txt"));
    service = new TextDocumentService(null, null, fsManager);
  }

  @Test
  public void getSimpleSnippet() {
    List<SnippetResult> result = getSnippets(26, 4, 27, 2);
    assertEquals(result.size(), 2);
    assertEquals(result.get(0).getSnippet(), "lines that");
    assertEquals(result.get(0).getRangeInSnippet(), new LinearRange(3, 4));
    assertEquals(result.get(1).getSnippet(), "lines that");
    assertEquals(result.get(1).getRangeInSnippet(), new LinearRange(4, 2));
  }

  @Test
  public void getCrossLineSnippet() {
    List<SnippetResult> result = getSnippets(26, 20);
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getSnippet(), "lines that");
    assertEquals(result.get(0).getRangeInSnippet(), new LinearRange(3, 7));
  }

  private List<SnippetResult> getSnippets(int... ranges) {
    List<LinearRange> r = new ArrayList<>(ranges.length / 2);
    for (int i = 0; i < ranges.length - 1; i += 2) {
      r.add(new LinearRange(ranges[i], ranges[i + 1]));
    }
    return service.getSnippets(new SnippetParameters("file:///projects/foo", r));
  }
}
