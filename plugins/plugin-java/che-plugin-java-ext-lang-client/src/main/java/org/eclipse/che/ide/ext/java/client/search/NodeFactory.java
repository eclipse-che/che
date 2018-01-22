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
