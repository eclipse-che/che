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
package org.eclipse.che.ide.ext.java.client.resource;

import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.RenamingSupport;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Disable rename operation for the children of the source folder.
 *
 * @author Valeriy Svydenko
 */
public class JavaSourceRenameValidator implements RenamingSupport {
  @Override
  public boolean isRenameAllowed(Resource resource) {
    boolean inSource = resource.getParentWithMarker(SourceFolderMarker.ID).isPresent();
    boolean isJavaClass = resource.isFile() && "java".equals(((File) resource).getExtension());

    return !inSource || !resource.isFolder() && !isJavaClass;
  }
}
