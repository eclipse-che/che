package org.eclipse.che.ide.ext.java.client.search;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ui.smartTree.data.AbstractTreeNode;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.presentation.HasNewPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NewNodePresentation;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

public class MatchNode extends AbstractTreeNode implements HasNewPresentation, HasAction {
  private String uri;
  private LinearRange range;
  private String snippet;
  private JavaResources resources;
  private OpenFileInEditorHelper openHelper;
  private PromiseProvider promiseProvider;

  @Inject
  public MatchNode(
      @Assisted("uri") String uri,
      @Assisted("range") LinearRange range,
      @Assisted("snippet") String snippet,
      JavaResources resources,
      NodeFactory nodeFactory,
      OpenFileInEditorHelper openHelper,
      PromiseProvider promiseProvider) {
    this.uri = uri;
    this.range = range;
    this.snippet = snippet;
    this.resources = resources;
    this.openHelper = openHelper;
    this.promiseProvider = promiseProvider;
  }

  @Override
  public String getName() {
    return snippet;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public NewNodePresentation getPresentation() {
    return new NewNodePresentation.Builder()
        .withIcon(resources.searchMatch())
        .withNodeText(snippet)
        .build();
  }

  @Override
  protected Promise<List<Node>> getChildrenImpl() {
    return promiseProvider.resolve(Collections.emptyList());
  }

  @Override
  public void actionPerformed() {
    openHelper.openLocation(uri, range);
  }
}
