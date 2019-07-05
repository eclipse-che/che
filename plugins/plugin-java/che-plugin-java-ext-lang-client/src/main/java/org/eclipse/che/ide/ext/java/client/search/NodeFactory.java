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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

public interface NodeFactory {
  UsagesNode createRoot(UsagesResponse response);

  ProjectNode createProject(String name, List<SearchResult> results);

  PackageNode createPackage(SearchResult pkg);

  ElementNode createElementNode(SearchResult result);

  MatchNode createMatch(@Assisted("uri") String uri, @Assisted("snippet") SnippetResult snippet);
}
