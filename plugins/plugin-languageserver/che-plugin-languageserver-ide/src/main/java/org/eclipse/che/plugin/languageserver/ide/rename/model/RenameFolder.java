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
public class RenameFolder {
  private final String name;
  private final List<RenameFile> files;

  public RenameFolder(String name, List<RenameFile> files) {
    this.name = name;
    this.files = files;
  }

  public String getName() {
    return name;
  }

  public List<RenameFile> getFiles() {
    return files;
  }
}
