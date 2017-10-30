/*
 * Copyright (c) 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;

/**
 * A LaunchingStrategy determines when and how an instance of a language server needs to be launched
 * for a given URI. Typically, this would be "one instance per workspace" or "one instance per
 * project"
 *
 * @author Thomas Mäder
 */
public interface LaunchingStrategy {
  /**
   * Compute a key that will identify a single launch of a language server
   *
   * @param fileUri the file that is about to be opened
   * @return
   */
  String getLaunchKey(String fileUri);

  /**
   * Compute the workspace root uri for the given file
   *
   * @param fileUri the file that is about to be opened
   * @return workspace root parameter for the language server instance
   * @throws LanguageServerException
   */
  String getRootUri(String fileUri) throws LanguageServerException;

  /**
   * Determine whether the language server instance is responsible for the given file. For all files
   * f that yield the same launch key l, the strategy must return true when asked {@link
   * #isApplicable(l, f)}
   *
   * @param launchKey the key the language server was launched for
   * @param fileUri the file about to be opened
   * @return true if the language server is responsible for this file.
   */
  boolean isApplicable(String launchKey, String fileUri);
}
