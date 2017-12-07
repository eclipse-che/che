package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.assistedinject.Assisted;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;

public interface NodeFactory {
  UsagesNode createRoot(UsagesResponse response);

  ProjectNode createProject(String name, List<SearchResult> results);

  PackageNode createPackage(SearchResult pkg);

  ElementNode createElementNode(SearchResult result);

  MatchNode createMatch(
      @Assisted("uri") String uri,
      @Assisted("range") LinearRange range,
      @Assisted("snippet") String snippet);
}
