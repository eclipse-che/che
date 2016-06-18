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
package org.eclipse.che.ide.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.QueryExpression;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;

import java.util.List;

/**
 * Presenter for full text search.
 *
 * @author Valeriy Svydenko
 * @author Roman Nikitenko
 */
@Singleton
public class FullTextSearchPresenter implements FullTextSearchView.ActionDelegate {
    private static final String URL_ENCODED_BACKSLASH = "%5C";
    private static final String AND_OPERATOR          = "AND";

    private final FullTextSearchView   view;
    private final FindResultPresenter  findResultPresenter;
    private final DtoFactory           dtoFactory;
    private final AppContext           appContext;
    private final ProjectServiceClient projectServiceClient;

    @Inject
    public FullTextSearchPresenter(FullTextSearchView view,
                                   FindResultPresenter findResultPresenter,
                                   DtoFactory dtoFactory,
                                   AppContext appContext,
                                   ProjectServiceClient projectServiceClient) {
        this.view = view;
        this.findResultPresenter = findResultPresenter;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.projectServiceClient = projectServiceClient;

        this.view.setDelegate(this);
    }

    /** Show dialog with view for searching. */
    public void showDialog() {
        view.showDialog();
        view.clearInput();
        if (appContext.getCurrentProject() != null) {
            view.setPathDirectory(appContext.getCurrentProject().getProjectConfig().getPath());
        } else {
            view.setPathDirectory("/");
        }
    }

    @Override
    public void search(final String text) {
        QueryExpression queryExpression = new QueryExpression();
        queryExpression.setText(prepareQuery(text));
        if (!view.getFileMask().isEmpty()) {
            queryExpression.setName(view.getFileMask());
        }
        if (!view.getPathToSearch().isEmpty()) {
            queryExpression.setPath(view.getPathToSearch());
        }

        projectServiceClient.search(appContext.getDevMachine(), queryExpression).then(new Operation<List<ItemReference>>() {
            @Override
            public void apply(List<ItemReference> result) throws OperationException {
                view.close();
                findResultPresenter.handleResponse(result, text);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                view.showErrorMessage(dtoFactory.createDtoFromJson(arg.getMessage(), ServiceError.class).getMessage());
            }
        });
    }

    private String prepareQuery(String text) {
        StringBuilder sb = new StringBuilder();
        for (char character : text.toCharArray()) {
            // Escape those characters that QueryParser expects to be escaped
            if (character == '\\' ||
                character == '+' ||
                character == '-' ||
                character == '!' ||
                character == '(' ||
                character == ')' ||
                character == ':' ||
                character == '^' ||
                character == '[' ||
                character == ']' ||
                character == '\"' ||
                character == '{' ||
                character == '}' ||
                character == '~' ||
                character == '*' ||
                character == '?' ||
                character == '|' ||
                character == '&' ||
                character == '/') {
                sb.append(URL_ENCODED_BACKSLASH);
            }
            sb.append(character);
        }
        String escapedText = sb.toString();

        String[] items = escapedText.trim().split("\\s+");
        int numberItem = items.length;
        if (numberItem == 1) {
            return items[0] + '*';
        }

        String lastItem = items[numberItem - 1];
        sb = new StringBuilder();
        sb.append('"');
        sb.append(escapedText.substring(0, escapedText.lastIndexOf(lastItem)));
        sb.append("\" " + AND_OPERATOR + " ");
        sb.append(lastItem);
        sb.append('*');
        return sb.toString();
    }

    @Override
    public void setPathDirectory(String path) {
        view.setPathDirectory(path);
    }

    @Override
    public void setFocus() {
        view.setFocus();
    }

    @Override
    public void onEnterClicked() {
        if (view.isAcceptButtonInFocus()) {
            String searchText = view.getSearchText();
            if (!searchText.isEmpty()) {
                search(searchText);
            }
            return;
        }

        if (view.isCancelButtonInFocus()) {
            view.close();
            return;
        }

        if (view.isSelectPathButtonInFocus()) {
            view.showSelectPathDialog();
        }
    }
}
