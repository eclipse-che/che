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
package org.eclipse.che.ide.ext.java.client.search.node;

import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.ext.java.shared.dto.model.ClassFile;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragment;
import org.eclipse.che.ide.ext.java.shared.dto.model.PackageFragmentRoot;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.ext.java.shared.dto.search.Match;

/**
 * Factory for creating tree element for search result tree.
 *
 * @author Evgen Vidolob
 */
public interface NodeFactory {

  ResultNode create(FindUsagesResponse response);

  JavaProjectNode create(JavaProject javaProject, Map<String, List<Match>> matches);

  PackageFragmentNode create(
      PackageFragment packageFragment,
      Map<String, List<Match>> matches,
      PackageFragmentRoot parent);

  TypeNode create(
      Type type,
      CompilationUnit compilationUnit,
      ClassFile classFile,
      Map<String, List<Match>> matches);

  MethodNode create(
      Method method,
      Map<String, List<Match>> matches,
      CompilationUnit compilationUnit,
      ClassFile classFile);

  MatchNode create(Match match, CompilationUnit compilationUnit, ClassFile classFile);
}
