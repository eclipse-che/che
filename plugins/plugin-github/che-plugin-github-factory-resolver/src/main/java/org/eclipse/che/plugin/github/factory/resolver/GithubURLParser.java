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
package org.eclipse.che.plugin.github.factory.resolver;

/**
 * Interface for Gitlab repository URL parsers.
 *
 * @author Max Shaposhnik
 */
public interface GithubURLParser {

  /**
   * Check if the URL is a valid Github url for the given provider.
   *
   * @param url a not null string representation of URL
   * @return {@code true} if the URL is a valid url for the given provider.
   */
  boolean isValid(String url);

  /**
   * Provides a parsed URL object of the given provider type.
   *
   * @param url URL to transform into a managed object
   * @return managed url object
   */
  GithubUrl parse(String url);
}
