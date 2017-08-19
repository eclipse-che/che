/**
 * ***************************************************************************** Copyright (c)
 * 2012-2016 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.tagging;

/** @author Dmitry Shnurenko */
public interface IRenameSubpackages {

  /**
   * If <code>canEnableRenameSubpackages</code> returns <code>true</code>, then this method is used
   * to inform the refactoring object that references in subpackages be updated. This call can be
   * ignored if <code>canEnableRenameSubpackages</code> returns <code>false</code>.
   */
  void setRenameSubpackages(boolean rename);
}
