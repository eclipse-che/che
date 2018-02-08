/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.project;

/** @author Artem Zatsarynnyi */
public class QueryExpression {
  private String name;
  private String path;
  private String text;
  private int maxItems;
  private int skipCount;

  /**
   * Get path to start search.
   *
   * @return path to start search
   */
  public String getPath() {
    return path;
  }

  /**
   * Set path to start search.
   *
   * @param path path to start search
   * @return this {@code QueryExpression}
   */
  public QueryExpression setPath(String path) {
    this.path = path;
    return this;
  }

  /**
   * Get name of file to search.
   *
   * @return file name to search
   */
  public String getName() {
    return name;
  }

  /**
   * Set name of file to search.
   *
   * <p>Supported wildcards are:
   *
   * <ul>
   *   <li><code>*</code>, which matches any character sequence (including the empty one);
   *   <li><code>?</code>, which matches any single character.
   * </ul>
   *
   * @param name file name to search
   * @return this {@code QueryExpression}
   */
  public QueryExpression setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get text to search.
   *
   * @return text to search
   */
  public String getText() {
    return text;
  }

  /**
   * Set text to search.
   *
   * @param text text to search
   * @return this {@code QueryExpression}
   */
  public QueryExpression setText(String text) {
    this.text = text;
    return this;
  }

  /**
   * Get maximum number of items in response.
   *
   * @return maximum number of items in response
   */
  public int getMaxItems() {
    return maxItems;
  }

  /**
   * Set maximum number of items in response.
   *
   * @param maxItems maximum number of items in response
   * @return this {@code QueryExpression}
   */
  public QueryExpression setMaxItems(int maxItems) {
    this.maxItems = maxItems;
    return this;
  }

  /**
   * Get amount of items to skip.
   *
   * @return amount of items to skip
   */
  public int getSkipCount() {
    return skipCount;
  }

  /**
   * Set amount of items to skip.
   *
   * @param skipCount amount of items to skip
   * @return this {@code QueryExpression}
   */
  public QueryExpression setSkipCount(int skipCount) {
    this.skipCount = skipCount;
    return this;
  }
}
