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
import org.eclipse.che.plugin.languageserver.ide.navigation.symbol.SymbolKind;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenModel;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenPresenter;
import org.eclipse.che.plugin.languageserver.ide.service.WorkspaceServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.SymbolInformationDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.WorkspaceSymbolParamsDTO;

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
   ;
    private final OpenFileInEditorHelper editorHelper;
    private final QuickOpenPresenter presenter;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final DtoFactory dtoFactory;
    private final EditorAgent editorAgent;
    private final SymbolKind symbolKind;
    private final FuzzyMatches fuzzyMatches;

    @Inject
    public FindSymbolAction(LanguageServerLocalization localization,
                            OpenFileInEditorHelper editorHelper,
                            QuickOpenPresenter presenter,
                            WorkspaceServiceClient workspaceServiceClient,
                            DtoFactory dtoFactory,
                            EditorAgent editorAgent,
                            SymbolKind symbolKind,
                            FuzzyMatches fuzzyMatches) {
        super(singletonList(PROJECT_PERSPECTIVE_ID), localization.findSymbolActionTitle(), localization.findSymbolActionTitle(), null,
              null);
        this.editorHelper = editorHelper;
        this.presenter = presenter;
        this.workspaceServiceClient = workspaceServiceClient;
        this.dtoFactory = dtoFactory;
        this.editorAgent = editorAgent;
        this.symbolKind = symbolKind;
        this.fuzzyMatches = fuzzyMatches;
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
    public Promise<QuickOpenModel> getModel(String value) {
        Promise<List<SymbolEntry>> promise;

        if (Strings.isNullOrEmpty(value)|| editorAgent.getActiveEditor() == null) {
            promise = Promises.resolve(Collections.<SymbolEntry>emptyList());
        } else {
            promise = searchSymbols(value);
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
            if(!SUPPORTED_OPEN_TYPES.contains(symbolKind.from(element.getKind()))){
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
                    result.add(new SymbolEntry(element.getName(), "", filePath, filePath, symbolKind.from(element.getKind()), range,
                                               symbolKind.getIcon(element.getKind()), editorHelper, matches));
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
