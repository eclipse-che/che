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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.search.presentation.FindResultPresenter;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Presenter for full text search.
 *
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 * @author Roman Nikitenko
 */
@Singleton
public class FullTextSearchPresenter implements FullTextSearchView.ActionDelegate {
    private static final String URL_ENCODED_BACKSLASH = "%5C";
    private static final String AND_OPERATOR          = "AND";

    private final FullTextSearchView  view;
    private final FindResultPresenter findResultPresenter;
    private final AppContext          appContext;
    private       Path                defaultStartPoint;

    @Inject
    public FullTextSearchPresenter(FullTextSearchView view,
                                   FindResultPresenter findResultPresenter,
                                   AppContext appContext) {
        this.view = view;
        this.findResultPresenter = findResultPresenter;
        this.appContext = appContext;

        this.view.setDelegate(this);
    }

    /** Show dialog with view for searching. */
    public void showDialog(Path path) {
        this.defaultStartPoint = path;

        view.showDialog();
        view.clearInput();
        view.setPathDirectory(path.toString());
    }

    @Override
    public void search(final String text) {
        final Path startPoint = isNullOrEmpty(view.getPathToSearch()) ? defaultStartPoint : Path.valueOf(view.getPathToSearch());

        appContext.getWorkspaceRoot().getContainer(startPoint).then(new Operation<Optional<Container>>() {
            @Override
            public void apply(Optional<Container> optionalContainer) throws OperationException {
                if (!optionalContainer.isPresent()) {
                    view.showErrorMessage("Path '" + startPoint + "' doesn't exists");
                    return;
                }

                final Container container = optionalContainer.get();
                container.search(view.getFileMask(), prepareQuery(text)).then(new Operation<Resource[]>() {
                    @Override
                    public void apply(Resource[] result) throws OperationException {
                        view.close();
                        findResultPresenter.handleResponse(result, text);
                    }
                });
            }
        });
    }

    //todo move this to the core part of resource manager
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
        String escapedText;
        if (view.isWholeWordsOnly()) {
            escapedText = sb.toString();
        } else {
            sb.append('*');
            escapedText = '*' + sb.toString();
        }

        String[] items = escapedText.trim().split("\\s+");
        int numberItem = items.length;
        if (numberItem == 1) {
            return items[0];
        }

        String lastItem = items[numberItem - 1];
        sb = new StringBuilder();
        sb.append('"');
        sb.append(escapedText.substring(0, escapedText.lastIndexOf(lastItem)));
        sb.append("\" " + AND_OPERATOR + " ");
        sb.append(lastItem);
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
