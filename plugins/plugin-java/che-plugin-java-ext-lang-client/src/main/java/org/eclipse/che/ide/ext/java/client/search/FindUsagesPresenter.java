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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.languageserver.shared.model.SnippetParameters;
import org.eclipse.che.api.languageserver.shared.model.SnippetResult;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.providers.DynaObject;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for Find Usages tree
 *
 * @author Evgen Vidolob
 */
@Singleton
@DynaObject
public class FindUsagesPresenter extends BasePresenter implements BaseActionDelegate {

  private WorkspaceAgent workspaceAgent;
  private JavaLocalizationConstant localizationConstant;
  private FindUsagesView view;
  private JavaLanguageExtensionServiceClient searchService;
  private TextDocumentServiceClient textDocumentService;
  private NotificationManager manager;
  private final Resources resources;
  private DtoBuildHelper dtoBuildHelper;
  private Map<String, Map<LinearRange, SnippetResult>> cache = new HashMap<>();
  private PromiseProvider promiseProvider;
  private NodeFactory nodeFactory;

  @Inject
  public FindUsagesPresenter(
      WorkspaceAgent workspaceAgent,
      JavaLocalizationConstant localizationConstant,
      FindUsagesView view,
      JavaLanguageExtensionServiceClient searchService,
      TextDocumentServiceClient textDocumentService,
      DtoBuildHelper dtoBuildHelper,
      NotificationManager manager,
      Resources resources,
      PromiseProvider promiseProvider,
      NodeFactory nodeFactory) {
    this.workspaceAgent = workspaceAgent;
    this.localizationConstant = localizationConstant;
    this.view = view;
    this.searchService = searchService;
    this.textDocumentService = textDocumentService;
    this.dtoBuildHelper = dtoBuildHelper;
    this.manager = manager;
    this.resources = resources;
    this.promiseProvider = promiseProvider;
    this.nodeFactory = nodeFactory;
    view.setDelegate(this);
  }

  @Override
  public String getTitle() {
    return localizationConstant.findUsagesPartTitle();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return localizationConstant.findUsagesPartTitleTooltip();
  }

  @Override
  public SVGResource getTitleImage() {
    return resources.find();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  public void findUsages(TextEditor editor) {
    if (!(editor.getConfiguration() instanceof LanguageServerEditorConfiguration)) {
      return;
    }

    Document document = editor.getDocument();
    TextDocumentPositionParams paramsDTO =
        dtoBuildHelper.createTDPP(document, editor.getCursorOffset());

    Promise<UsagesResponse> promise = searchService.usages(paramsDTO);
    promise
        .then(this::handleResponse)
        .catchError(
            arg -> {
              Throwable cause = arg.getCause();
              Log.error(getClass(), cause);
              manager.notify(
                  localizationConstant.failedToProcessFindUsage(),
                  arg.getMessage(),
                  FAIL,
                  FLOAT_MODE);
            });
  }

  private void handleResponse(UsagesResponse response) {
    workspaceAgent.openPart(this, PartStackType.INFORMATION);
    workspaceAgent.setActivePart(this);
    cache.clear();
    view.showUsages(response);
  }

  public Promise<List<MatchNode>> computeMatches(ElementNode elementNode) {
    Node ancestor = elementNode.getParent();
    SearchResult rootElement = elementNode.getElement();

    while ((ancestor instanceof ElementNode)
        && ((ElementNode) ancestor)
            .getElement()
            .getUri()
            .equals(elementNode.getElement().getUri())) {
      rootElement = ((ElementNode) ancestor).getElement();
      ancestor = ancestor.getParent();
    }

    return loadMatches(rootElement)
        .then(
            (Function<Map<LinearRange, SnippetResult>, List<MatchNode>>)
                matches -> {
                  return filterMatches(elementNode.getElement(), matches);
                });
  }

  private List<MatchNode> filterMatches(SearchResult sr, Map<LinearRange, SnippetResult> matches) {
    return sr.getMatches()
        .stream()
        .filter(
            range -> {
              return matches.containsKey(range);
            })
        .map(range -> nodeFactory.createMatch(sr.getUri(), matches.get(range)))
        .collect(Collectors.toList());
  }

  private Promise<Map<LinearRange, SnippetResult>> loadMatches(SearchResult element) {
    Map<LinearRange, SnippetResult> snippets = cache.get(element.getUri());
    if (snippets != null) {
      return promiseProvider.resolve(snippets);
    }

    List<LinearRange> ranges = new ArrayList<>();
    collectMatchRanges(element, ranges);
    SnippetParameters params =
        new org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.SnippetParametersDto(
            new SnippetParameters(element.getUri(), ranges));

    return textDocumentService
        .getSnippets(params)
        .then(
            (List<SnippetResult> results) -> {
              Map<LinearRange, SnippetResult> snippetMap = new HashMap<>();
              cache.put(element.getUri(), snippets);
              results.forEach(
                  result -> {
                    snippetMap.put(result.getLinearRange(), result);
                  });
              return snippetMap;
            });
  }

  private void collectMatchRanges(SearchResult element, List<LinearRange> ranges) {
    ranges.addAll(element.getMatches());
    element.getChildren().forEach(child -> collectMatchRanges(child, ranges));
  }
}
