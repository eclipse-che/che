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
package org.eclipse.che.ide.editor;

import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vitaly Parfonov */
final class EditorInputImpl implements EditorInput {
  private VirtualFile file;
  private FileType fileType;

  EditorInputImpl(FileType fileType, VirtualFile file) {
    this.fileType = fileType;
    this.file = file;
  }

  @Override
  public String getToolTipText() {
    return null;
  }

  @Override
  public String getName() {
    return file.getDisplayName();
  }

  @Override
  public SVGResource getSVGResource() {
    return fileType.getImage();
  }

  @Override
  public VirtualFile getFile() {
    return file;
  }

  @Override
  public void setFile(VirtualFile file) {
    this.file = file;
  }
}
