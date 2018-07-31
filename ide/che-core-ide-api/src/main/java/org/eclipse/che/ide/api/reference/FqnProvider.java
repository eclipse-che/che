/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.reference;

/**
 * The class provides methods which allows to extract fully qualified name for nodes. Fully
 * qualified name (Fqn) is an unambiguous name that specifies which object, function, or variable a
 * call refers to without regard to the context of the call. In a hierarchical structure, a name is
 * fully qualified when it "is complete in the sense that it includes (a) all names in the
 * hierarchic sequence above the given element and (b) the name of the given element itself." Thus
 * fully qualified names explicitly refer to namespaces that would otherwise be implicit because of
 * the scope of the call.
 *
 * @author Dmitry Shnurenko
 */
public interface FqnProvider {

  /**
   * The methods extracts fqn from nodes which contains it. If node doesn't contain fqn, method
   * returns empty string.
   *
   * @param object node for which fqn will be extract
   * @return string representation of fqn or empty string if node doesn't contain fqn.
   */
  String getFqn(Object object);
}
