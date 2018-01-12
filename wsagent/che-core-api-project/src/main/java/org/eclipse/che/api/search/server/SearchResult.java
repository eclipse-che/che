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
package org.eclipse.che.api.search.server;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Optional;
import java.util.List;
import org.eclipse.che.api.search.server.impl.QueryExpression;
import org.eclipse.che.api.search.server.impl.SearchResultEntry;

/** Result of executing {@link Searcher#search(QueryExpression)}. */
public class SearchResult {
  public static SearchResultBuilder aSearchResult() {
    return new SearchResultBuilder();
  }

  private final List<SearchResultEntry> results;
  private final Optional<QueryExpression> nextPageQueryExpression;
  private final long totalHits;
  private final long elapsedTimeMillis;

  private SearchResult(
      List<SearchResultEntry> results,
      Optional<QueryExpression> nextPageQueryExpression,
      long totalHits,
      long elapsedTimeMillis) {
    this.results = results;
    this.nextPageQueryExpression = nextPageQueryExpression;
    this.totalHits = totalHits;
    this.elapsedTimeMillis = elapsedTimeMillis;
  }

  /**
   * Paths of files that match the search criteria. This method is shortcut for:
   *
   * <pre>{@code
   * SearchResult result = ...;
   * List<String> paths = new ArrayList<>();
   * for (SearchResultEntry e : result.getResults()) {
   *    paths.add(e.getFilePath());
   * }
   * }</pre>
   */
  public List<String> getFilePaths() {
    return results.stream().map(SearchResultEntry::getFilePath).collect(toList());
  }

  /** Get all results that match the search criteria. */
  public List<SearchResultEntry> getResults() {
    return results;
  }

  /** Total number of files that match the search criteria. */
  public long getTotalHits() {
    return totalHits;
  }

  /** Time spent on execution the query. */
  public long getElapsedTimeMillis() {
    return elapsedTimeMillis;
  }

  /** Optional query expression for retrieving next page. */
  public Optional<QueryExpression> getNextPageQueryExpression() {
    return nextPageQueryExpression;
  }

  public static class SearchResultBuilder {
    private QueryExpression nextPageQueryExpression;
    private List<SearchResultEntry> results;
    private long totalHits;
    private long elapsedTimeMillis;

    private SearchResultBuilder() {}

    public SearchResultBuilder withNextPageQueryExpression(
        QueryExpression nextPageQueryExpression) {
      this.nextPageQueryExpression = nextPageQueryExpression;
      return this;
    }

    public SearchResultBuilder withResults(List<SearchResultEntry> results) {
      this.results = results;
      return this;
    }

    public SearchResultBuilder withTotalHits(long totalHits) {
      this.totalHits = totalHits;
      return this;
    }

    public SearchResultBuilder withElapsedTimeMillis(long elapsedTimeMillis) {
      this.elapsedTimeMillis = elapsedTimeMillis;
      return this;
    }

    public SearchResult build() {
      Optional<QueryExpression> optionalPageNexQueryExpression;
      if (nextPageQueryExpression == null) {
        optionalPageNexQueryExpression = Optional.absent();
      } else {
        optionalPageNexQueryExpression = Optional.of(nextPageQueryExpression);
      }
      if (results == null) {
        results = emptyList();
      }
      return new SearchResult(
          results, optionalPageNexQueryExpression, totalHits, elapsedTimeMillis);
    }
  }
}
