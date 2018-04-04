/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.reorg;

import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

class ArrayTypeConverter {

  private ArrayTypeConverter() {}

  static IFile[] toFileArray(Object[] objects) {
    List<?> l = Arrays.asList(objects);
    return l.toArray(new IFile[l.size()]);
  }

  static IFolder[] toFolderArray(Object[] objects) {
    List<?> l = Arrays.asList(objects);
    return l.toArray(new IFolder[l.size()]);
  }

  static ICompilationUnit[] toCuArray(Object[] objects) {
    List<?> l = Arrays.asList(objects);
    return l.toArray(new ICompilationUnit[l.size()]);
  }

  static IPackageFragmentRoot[] toPackageFragmentRootArray(Object[] objects) {
    List<?> l = Arrays.asList(objects);
    return l.toArray(new IPackageFragmentRoot[l.size()]);
  }

  static IPackageFragment[] toPackageArray(Object[] objects) {
    List<?> l = Arrays.asList(objects);
    return l.toArray(new IPackageFragment[l.size()]);
  }
}
