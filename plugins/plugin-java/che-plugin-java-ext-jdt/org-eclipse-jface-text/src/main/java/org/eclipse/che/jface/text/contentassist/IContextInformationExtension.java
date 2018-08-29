/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jface.text.contentassist;

/**
 * Extends {@link IContextInformation} with the ability to freely position the context information.
 *
 * @since 2.0
 */
public interface IContextInformationExtension {

  /**
   * Returns the start offset of the range for which this context information is valid or <code>-1
   * </code> if unknown.
   *
   * @return the start offset of the range for which this context information is valid
   */
  int getContextInformationPosition();
}
