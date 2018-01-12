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

import java.util.Map;

/**
 * Result of the LS 'rename' call. Contains results from all LS bind to the file type, client must
 * chose only one.
 */
public class RenameResult {

  private Map<String, ExtendedWorkspaceEdit> renameResults;

  public RenameResult() {}

  public RenameResult(Map<String, ExtendedWorkspaceEdit> renameResults) {
    this.renameResults = renameResults;
  }

  public Map<String, ExtendedWorkspaceEdit> getRenameResults() {
    return renameResults;
  }

  public void setRenameResults(Map<String, ExtendedWorkspaceEdit> renameResults) {
    this.renameResults = renameResults;
  }
}
