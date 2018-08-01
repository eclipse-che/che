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
package org.eclipse.che.api.search.server.impl;

import java.util.List;
import org.eclipse.che.api.search.server.OffsetData;

/** Single item in {@code SearchResult}. */
public class SearchResultEntry {
  private final String filePath;

  private final List<OffsetData> data;

  public SearchResultEntry(String filePath, List<OffsetData> data) {
    this.filePath = filePath;
    this.data = data;
  }

  public List<OffsetData> getData() {
    return data;
  }

  /** Path of file that matches the search criteria. */
  public String getFilePath() {
    return filePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SearchResultEntry)) {
      return false;
    }

    SearchResultEntry that = (SearchResultEntry) o;

    if (getFilePath() != null
        ? !getFilePath().equals(that.getFilePath())
        : that.getFilePath() != null) {
      return false;
    }
    return getData() != null ? getData().equals(that.getData()) : that.getData() == null;
  }

  @Override
  public int hashCode() {
    int result = getFilePath() != null ? getFilePath().hashCode() : 0;
    result = 31 * result + (getData() != null ? getData().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SearchResultEntry{" + "filePath='" + filePath + '\'' + ", data=" + data + '}';
  }
}
