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
package org.eclipse.che.ide.ext.java.client.tree;

import com.google.common.annotations.Beta;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFolderNode;
import org.eclipse.che.ide.ext.java.client.tree.library.JarNode;
import org.eclipse.che.ide.ext.java.client.tree.library.LibrariesNode;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode.NodeFactory;
import org.eclipse.che.ide.ui.smartTree.data.settings.NodeSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;

/** @author Vlad Zhukovskiy */
@Beta
public interface JavaNodeFactory extends NodeFactory {
  PackageNode newPackage(Container resource, NodeSettings nodeSettings);

  LibrariesNode newLibrariesNode(Path project, NodeSettings nodeSettings);

  JarNode newJarNode(Jar jar, Path project, NodeSettings nodeSettings);

  JarFolderNode newJarFolderNode(
      JarEntry jarEntry, String libId, Path project, NodeSettings nodeSettings);

  JarFileNode newJarFileNode(
      JarEntry jarEntry, String libId, Path project, NodeSettings nodeSettings);
}
