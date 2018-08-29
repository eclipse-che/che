/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javaeditor;

/** Semantic highlighting */
public abstract class SemanticHighlighting {

  /** @return the display name */
  public abstract String getType();

  /**
   * Returns <code>true</code> iff the semantic highlighting consumes the semantic token.
   *
   * <p>NOTE: Implementors are not allowed to keep a reference on the token or on any object
   * retrieved from the token.
   *
   * @param token the semantic token for a {@link org.eclipse.jdt.core.dom.SimpleName}
   * @return <code>true</code> iff the semantic highlighting consumes the semantic token
   */
  public abstract boolean consumes(SemanticToken token);

  /**
   * Returns <code>true</code> iff the semantic highlighting consumes the semantic token.
   *
   * <p>NOTE: Implementors are not allowed to keep a reference on the token or on any object
   * retrieved from the token.
   *
   * @param token the semantic token for a {@link org.eclipse.jdt.core.dom.NumberLiteral}, {@link
   *     org.eclipse.jdt.core.dom.BooleanLiteral} or {@link
   *     org.eclipse.jdt.core.dom.CharacterLiteral}
   * @return <code>true</code> iff the semantic highlighting consumes the semantic token
   */
  public boolean consumesLiteral(SemanticToken token) {
    return false;
  }
}
