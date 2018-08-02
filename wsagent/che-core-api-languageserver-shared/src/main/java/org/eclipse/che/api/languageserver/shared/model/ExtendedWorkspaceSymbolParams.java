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

import org.eclipse.lsp4j.WorkspaceSymbolParams;

/**
 * Version of workspace symbol params that holds the uri of the file the ide has open.
 *
 * @author Thomas Mäder
 */
public class ExtendedWorkspaceSymbolParams extends WorkspaceSymbolParams {

  private String fileUri;

  public String getFileUri() {
    return fileUri;
  }

  public void setFileUri(String fileUri) {
    this.fileUri = fileUri;
  }
}
