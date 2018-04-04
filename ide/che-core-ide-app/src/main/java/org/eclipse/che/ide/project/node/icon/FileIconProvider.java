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
package org.eclipse.che.ide.project.node.icon;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Resolve icon based on registered file type.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class FileIconProvider implements NodeIconProvider {

  private final FileTypeRegistry fileTypeRegistry;
  private final FileType unknownFileType;

  @Inject
  public FileIconProvider(
      FileTypeRegistry fileTypeRegistry, @Named("defaultFileType") FileType unknownFileType) {
    this.fileTypeRegistry = fileTypeRegistry;
    this.unknownFileType = unknownFileType;
  }

  @Override
  public SVGResource getIcon(Resource resource) {

    if (resource.getResourceType() != Resource.FILE) {
      return null;
    }

    FileType fileType = fileTypeRegistry.getFileTypeByFile((File) resource);

    return fileType.equals(unknownFileType) ? null : fileType.getImage();
  }
}
