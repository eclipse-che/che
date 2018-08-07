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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;

public class SnippetParameters {
  private String uri;
  private List<LinearRange> ranges;

  public SnippetParameters() {}

  public SnippetParameters(String uri, List<LinearRange> edits) {
    this.uri = uri;
    this.ranges = edits;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public List<LinearRange> getRanges() {
    return ranges;
  }

  public void setRanges(List<LinearRange> ranges) {
    this.ranges = new ArrayList<>(ranges);
  }
}
