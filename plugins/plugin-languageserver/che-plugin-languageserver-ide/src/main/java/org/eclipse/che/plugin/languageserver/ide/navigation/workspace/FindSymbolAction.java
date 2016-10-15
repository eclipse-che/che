/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.navigation.workspace;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.SymbolInformationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.WorkspaceSymbolParamsDTO;
import org.eclipse.che.api.promises.async.Task;
import org.eclipse.che.api.promises.async.ThrottledDelayer;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;
import org.eclipse.che.plugin.languageserver.ide.filters.FuzzyMatches;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.SymbolKindHelper;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenModel;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenPresenter;
import org.eclipse.che.plugin.languageserver.ide.service.WorkspaceServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class FindSymbolAction extends AbstractPerspectiveAction implements QuickOpenPresenter.QuickOpenPresenterOpts {

    private static final Set<String> SUPPORTED_OPEN_TYPES = Sets.newHashSet("class", "interface", "enum","function", "method");
    private static final int SEARCH_DELAY = 500;

    private final OpenFileInEditorHelper editorHelper;
    private final QuickOpenPresenter     presenter;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final DtoFactory             dtoFactory;
    private final EditorAgent            editorAgent;
    private final SymbolKindHelper       symbolKindHelper;
    private final FuzzyMatches           fuzzyMatches;
    private final ThrottledDelayer<List<SymbolEntry>> delayer;

    @Inject
    public FindSymbolAction(LanguageServerLocalization localization,
                            OpenFileInEditorHelper editorHelper,
                            QuickOpenPresenter presenter,
                            WorkspaceServiceClient workspaceServiceClient,
                            DtoFactory dtoFactory,
                            EditorAgent editorAgent,
                            SymbolKindHelper symbolKindHelper,
                            FuzzyMatches fuzzyMatches) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), localization.findSymbolActionTitle(), localization.findSymbolActionTitle(), null,
              null);
        this.editorHelper = editorHelper;
        this.presenter = presenter;
        this.workspaceServiceClient = workspaceServiceClient;
        this.dtoFactory = dtoFactory;
        this.editorAgent = editorAgent;
        this.symbolKindHelper = symbolKindHelper;
        this.fuzzyMatches = fuzzyMatches;
        this.delayer = new ThrottledDelayer<>(SEARCH_DELAY);
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        presenter.run(this);
    }

    @Override
    public Promise<QuickOpenModel> getModel(final String value) {
        Promise<List<SymbolEntry>> promise;

        if (Strings.isNullOrEmpty(value)|| editorAgent.getActiveEditor() == null) {
            promise = Promises.resolve(Collections.<SymbolEntry>emptyList());
        } else {
            promise = delayer.trigger(new Task<Promise<List<SymbolEntry>>>() {
                @Override
                public Promise<List<SymbolEntry>> run() {
                    return searchSymbols(value);
                }
            });
        }
        return promise.then(new Function<List<SymbolEntry>, QuickOpenModel>() {
            @Override
            public QuickOpenModel apply(List<SymbolEntry> arg) throws FunctionException {
                return new QuickOpenModel(arg);
            }
        });
    }

    private Promise<List<SymbolEntry>> searchSymbols(final String value) {
        WorkspaceSymbolParamsDTO params = dtoFactory.createDto(WorkspaceSymbolParamsDTO.class);
        params.setQuery(value);
        params.setFileUri(editorAgent.getActiveEditor().getEditorInput().getFile().getPath());
        return workspaceServiceClient.symbol(params).then(new Function<List<SymbolInformationDTO>, List<SymbolEntry>>() {
            @Override
            public List<SymbolEntry> apply(List<SymbolInformationDTO> types) throws FunctionException {
                return toSymbolEntries(types, value);
            }
        });
    }

    private List<SymbolEntry> toSymbolEntries(List<SymbolInformationDTO> types, String value) {
        List<SymbolEntry> result = new ArrayList<>();
        for (SymbolInformationDTO element : types) {
            if(!SUPPORTED_OPEN_TYPES.contains(symbolKindHelper.from(element.getKind()))){
                continue;
            }
            List<Match> matches = fuzzyMatches.fuzzyMatch(value, element.getName());
            if (matches != null) {
                LocationDTO location = element.getLocation();
                if (location != null && location.getUri() != null) {
                    String filePath = location.getUri();
                    RangeDTO locationRange = location.getRange();

                    TextRange range = null;
                    if (locationRange != null) {
                        range = new TextRange(new TextPosition(locationRange.getStart().getLine(), locationRange.getStart().getCharacter()),
                                              new TextPosition(locationRange.getEnd().getLine(), locationRange.getEnd().getCharacter()));

                    }
                    result.add(new SymbolEntry(element.getName(), "", filePath, filePath, symbolKindHelper.from(element.getKind()), range,
                                               symbolKindHelper.getIcon(element.getKind()), editorHelper, matches));
                }
            }

        }
        //TODO add sorting
        return result;
    }

    @Override
    public void onClose(boolean canceled) {

    }
}
