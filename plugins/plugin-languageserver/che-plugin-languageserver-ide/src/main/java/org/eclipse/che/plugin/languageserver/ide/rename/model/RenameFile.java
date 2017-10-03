/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.plugin.languageserver.ide.rename.model;

import java.util.List;

/** */
public class RenameFile {

  private final String fileName;
  private final List<RenameChange> changes;

  public RenameFile(String fileName, List<RenameChange> changes) {
    this.fileName = fileName;
    this.changes = changes;
  }

  public String getFileName() {
    return fileName;
  }

  public List<RenameChange> getChanges() {
    return changes;
  }
}
