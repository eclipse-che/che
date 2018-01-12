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
package org.eclipse.che.plugin.languageserver.ide.rename.node;

import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameChange;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFile;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFolder;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameProject;

/** Factory for all rename nodes */
public interface RenameNodeFactory {

  ProjectNode create(RenameProject project);

  FolderNode create(RenameFolder folder);

  FileNode create(RenameFile file);

  ChangeNode create(RenameChange change);
}
