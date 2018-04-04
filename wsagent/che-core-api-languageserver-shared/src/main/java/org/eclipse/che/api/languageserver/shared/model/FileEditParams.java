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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.TextEdit;

public class FileEditParams {
  /** The workspace relative path of the file */
  private String uri;
  /** A list of TextEdits to be applied to the file */
  private List<TextEdit> edits;

  public FileEditParams() {}

  public FileEditParams(String path, List<TextEdit> edits) {
    this.uri = path;
    this.edits = edits;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public List<TextEdit> getEdits() {
    return edits;
  }

  public void setEdits(List<TextEdit> edits) {
    this.edits = new ArrayList<>(edits);
  }
}
