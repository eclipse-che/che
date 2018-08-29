/**
 * ***************************************************************************** Copyright (c)
 * 2012-2014 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.internal.core.builder;

import org.eclipse.jdt.internal.codeassist.ISearchRequestor;

public abstract class CodenvyClasspathLocation
    extends org.eclipse.jdt.internal.core.builder.ClasspathLocation {

  public abstract void findPackages(String[] pkgName, ISearchRequestor requestor);
}
