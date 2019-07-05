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
package org.eclipse.che.plugin.languageserver.ide.navigation.workspace;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceSymbolParams;
import org.eclipse.che.api.promises.async.ThrottledDelayer;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.editorconfig.TextEditorConfiguration;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.filters.FuzzyMatches;
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;
import org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.SymbolKindHelper;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenModel;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenPresenter;
import org.eclipse.che.plugin.languageserver.ide.service.WorkspaceServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;

/** @author Evgen Vidolob */
@Singleton
public class FindSymbolAction extends AbstractPerspectiveAction
    implements QuickOpenPresenter.QuickOpenPresenterOpts {

  private static final Set<String> SUPPORTED_OPEN_TYPES =
      Sets.newHashSet("class", "interface", "enum", "function", "method");
  private static final int SEARCH_DELAY = 500;

  private final OpenFileInEditorHelper editorHelper;
  private final QuickOpenPresenter presenter;
  private final WorkspaceServiceClient workspaceServiceClient;
  private final DtoFactory dtoFactory;
  private final EditorAgent editorAgent;
  private final SymbolKindHelper symbolKindHelper;
  private final FuzzyMatches fuzzyMatches;
  private PromiseProvider promiseProvider;
  private final ThrottledDelayer<List<SymbolEntry>> delayer;
  private DtoBuildHelper dtoHelper;

  @Inject
  public FindSymbolAction(
      LanguageServerLocalization localization,
      OpenFileInEditorHelper editorHelper,
      QuickOpenPresenter presenter,
      WorkspaceServiceClient workspaceServiceClient,
      DtoFactory dtoFactory,
      DtoBuildHelper dtoHelper,
      EditorAgent editorAgent,
      SymbolKindHelper symbolKindHelper,
      FuzzyMatches fuzzyMatches,
      PromiseProvider promiseProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.findSymbolActionTitle(),
        localization.findSymbolActionTitle());
    this.editorHelper = editorHelper;
    this.presenter = presenter;
    this.workspaceServiceClient = workspaceServiceClient;
    this.dtoFactory = dtoFactory;
    this.dtoHelper = dtoHelper;
    this.editorAgent = editorAgent;
    this.symbolKindHelper = symbolKindHelper;
    this.fuzzyMatches = fuzzyMatches;
    this.promiseProvider = promiseProvider;
    this.delayer = new ThrottledDelayer<>(SEARCH_DELAY);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (Objects.nonNull(activeEditor) && activeEditor instanceof TextEditor) {
      TextEditorConfiguration configuration = ((TextEditor) activeEditor).getConfiguration();
      if (configuration instanceof LanguageServerEditorConfiguration) {
        ServerCapabilities capabilities =
            ((LanguageServerEditorConfiguration) configuration).getServerCapabilities();
        event
            .getPresentation()
            .setEnabledAndVisible(
                capabilities.getWorkspaceSymbolProvider() != null
                    && capabilities.getWorkspaceSymbolProvider());
        return;
      }
    }
    event.getPresentation().setEnabledAndVisible(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    presenter.run(this);
  }

  @Override
  public Promise<QuickOpenModel> getModel(final String value) {
    Promise<List<SymbolEntry>> promise;

    if (Strings.isNullOrEmpty(value) || editorAgent.getActiveEditor() == null) {
      promise = promiseProvider.resolve(Collections.<SymbolEntry>emptyList());
    } else {
      promise = delayer.trigger(() -> searchSymbols(value));
    }
    return promise.then(
        new Function<List<SymbolEntry>, QuickOpenModel>() {
          @Override
          public QuickOpenModel apply(List<SymbolEntry> arg) throws FunctionException {
            return new QuickOpenModel(arg);
          }
        });
  }

  private Promise<List<SymbolEntry>> searchSymbols(final String value) {
    ExtendedWorkspaceSymbolParams params =
        dtoFactory.createDto(ExtendedWorkspaceSymbolParams.class);
    params.setQuery(value);
    params.setFileUri(dtoHelper.getUri(editorAgent.getActiveEditor().getEditorInput().getFile()));
    return workspaceServiceClient
        .symbol(params)
        .then(
            new Function<List<SymbolInformation>, List<SymbolEntry>>() {
              @Override
              public List<SymbolEntry> apply(List<SymbolInformation> types)
                  throws FunctionException {
                return toSymbolEntries(types, value);
              }
            });
  }

  private List<SymbolEntry> toSymbolEntries(List<SymbolInformation> types, String value) {
    List<SymbolEntry> result = new ArrayList<>();
    for (SymbolInformation element : types) {
      if (!SUPPORTED_OPEN_TYPES.contains(symbolKindHelper.from(element.getKind()))) {
        continue;
      }
      List<Match> matches = fuzzyMatches.fuzzyMatch(value, element.getName());
      if (matches != null) {
        Location location = element.getLocation();
        if (location != null && location.getUri() != null) {
          String filePath = location.getUri();
          result.add(
              new SymbolEntry(
                  element.getName(),
                  "",
                  filePath,
                  location,
                  symbolKindHelper.from(element.getKind()),
                  symbolKindHelper.getIcon(element.getKind()),
                  editorHelper,
                  matches));
        }
      }
    }
    // TODO add sorting
    return result;
  }

  @Override
  public void onClose(boolean canceled) {}
}
