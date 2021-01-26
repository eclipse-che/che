/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.bitbucket.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * Bitbucket's paging object. Combines collections of items with some metadata.
 *
 * <p>See more
 *
 * <p>https://docs.atlassian.com/bitbucket-server/rest/5.6.1/bitbucket-rest.html
 *
 * @param <T>
 */
public class Page<T> {
  private int start;
  private int size;
  private int limit;

  @JsonProperty(value = "isLastPage")
  private boolean isLastPage;

  private int nextPageStart;
  List<T> values;

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public boolean isLastPage() {
    return isLastPage;
  }

  public void setLastPage(boolean lastPage) {
    isLastPage = lastPage;
  }

  public List<T> getValues() {
    return values;
  }

  public void setValues(List<T> values) {
    this.values = values;
  }

  public int getNextPageStart() {
    return nextPageStart;
  }

  public void setNextPageStart(int nextPageStart) {
    this.nextPageStart = nextPageStart;
  }

  @Override
  public String toString() {
    return "Page{"
        + "start="
        + start
        + ", size="
        + size
        + ", limit="
        + limit
        + ", isLastPage="
        + isLastPage
        + ", nextPageStart="
        + nextPageStart
        + ", values="
        + values
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Page<?> page = (Page<?>) o;
    return start == page.start
        && size == page.size
        && limit == page.limit
        && isLastPage == page.isLastPage
        && nextPageStart == page.nextPageStart
        && Objects.equals(values, page.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, size, limit, isLastPage, nextPageStart, values);
  }
}
