/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.search;

import org.eclipse.search.ui.text.Match;

/**
 * A callback interface to report matches against. This class serves as a bottleneck and minimal
 * interface to report matches to the Java search infrastructure. Query participants will be passed
 * an instance of this interface when their <code>search(...)</code> method is called.
 *
 * <p>This interface is not intended to be implemented by clients.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISearchRequestor {
  /**
   * Adds a match to the search that issued this particular {@link ISearchRequestor}.
   *
   * @param match The match to be reported.
   */
  void reportMatch(Match match);
}
