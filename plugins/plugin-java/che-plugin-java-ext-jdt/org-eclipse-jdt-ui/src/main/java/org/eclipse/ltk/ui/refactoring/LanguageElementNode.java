/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.internal.ui.refactoring.InternalLanguageElementNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;

/**
 * A special child node of a <code>TextEditChangeNode</code> to represent language elements which
 * don't have an associated <code>TextEditChangeGroup
 * </code>. Instances of this class typically represent language members like types, methods,
 * fields, etc. in the change preview tree.
 *
 * <p>Clients may extend this class.
 *
 * @since 3.2
 */
public abstract class LanguageElementNode extends InternalLanguageElementNode {

  /**
   * Creates a new <code>LanguageElementNode</code> using the given <code>TextEditChangeGroup</code>
   * as a parent.
   *
   * @param parent the parent of this node
   */
  protected LanguageElementNode(TextEditChangeNode parent) {
    super(parent);
  }

  /**
   * Creates a new <code>LanguageElementNode</code> using the given <code>ChildNode</code> as a
   * parent.
   *
   * @param parent the parent of this node
   */
  protected LanguageElementNode(ChildNode parent) {
    super(parent);
  }

  /**
   * Adds the given <code>ChildNode<code> to this <code>LanguageElementNode</code>
   *
   * @param child the child to add
   */
  public void addChild(ChildNode child) {
    internalAddChild(child);
  }

  /**
   * Returns the text region the of this language element node.
   *
   * @return the text region of this language element node
   * @throws CoreException if the source region can't be obtained
   */
  public abstract IRegion getTextRange() throws CoreException;

  /**
   * This is an internal method which should not be called by subclasses.
   *
   * @param child the child node to add
   * @noreference This method is not intended to be referenced by clients.
   */
  protected void internalAddChild(ChildNode child) {
    super.internalAddChild(child);
  }
}
