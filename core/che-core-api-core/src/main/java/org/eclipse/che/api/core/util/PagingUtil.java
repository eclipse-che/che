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
package org.eclipse.che.api.core.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides useful methods for working with {@link Page} instances and pageable uris management.
 *
 * @author Yevhenii Voevodin
 */
public final class PagingUtil {

  private static final String LINK_HEADER_SEPARATOR = ", ";
  /**
   * Helps to retrieve href along with rel from link header value part. Value format is {@literal
   * <http://host:port/path?query=value>; rel="next"}, so the first group is href while the second
   * group is rel.
   */
  private static final Pattern LINK_HEADER_REGEX =
      Pattern.compile("<(?<href>.+)>;.*rel=\"(?<rel>.+)\".*");

  /**
   * Generates link header value from the page object and base uri. <a
   * href="https://tools.ietf.org/html/rfc5988">The Link header spec</a>
   *
   * @param page the page used to generate link
   * @param uri the uri which is used for adding {@code skipCount} & {@code maxItems} query
   *     parameters
   * @return 'Link' header value
   * @throws NullPointerException when either {@code page} or {@code uri} is null
   */
  public static String createLinkHeader(Page<?> page, URI uri) {
    requireNonNull(page, "Required non-null page");
    requireNonNull(uri, "Required non-null uri");
    final ArrayList<Pair<String, Page.PageRef>> pageRefs = new ArrayList<>(4);
    pageRefs.add(Pair.of("first", page.getFirstPageRef()));
    pageRefs.add(Pair.of("last", page.getLastPageRef()));
    if (page.hasPreviousPage()) {
      pageRefs.add(Pair.of("prev", page.getPreviousPageRef()));
    }
    if (page.hasNextPage()) {
      pageRefs.add(Pair.of("next", page.getNextPageRef()));
    }
    final UriBuilder ub = UriBuilder.fromUri(uri);
    return pageRefs
        .stream()
        .map(
            refPair ->
                format(
                    "<%s>; rel=\"%s\"",
                    ub.clone()
                        .replaceQueryParam("skipCount", refPair.second.getItemsBefore())
                        .replaceQueryParam("maxItems", refPair.second.getPageSize())
                        .build()
                        .toString(),
                    refPair.first))
        .collect(joining(LINK_HEADER_SEPARATOR));
  }

  /**
   * Returns REL to URI map based on the given {@code linkHeader} value. If the {@code linkHeader}
   * is null or empty then an empty map will be returned.
   *
   * <p>Note that link header is parsed due to the {@link #createLinkHeader(Page, URI)} method
   * strategy.
   *
   * @param linkHeader link header value
   */
  public static Map<String, String> parseLinkHeader(String linkHeader) {
    if (isNullOrEmpty(linkHeader)) {
      return emptyMap();
    }
    final Map<String, String> res = new HashMap<>();
    for (String part : linkHeader.split(LINK_HEADER_SEPARATOR)) {
      final Matcher matcher = LINK_HEADER_REGEX.matcher(part);
      if (matcher.matches()) {
        res.put(matcher.group("rel"), matcher.group("href"));
      }
    }
    return res;
  }

  private PagingUtil() {}
}
