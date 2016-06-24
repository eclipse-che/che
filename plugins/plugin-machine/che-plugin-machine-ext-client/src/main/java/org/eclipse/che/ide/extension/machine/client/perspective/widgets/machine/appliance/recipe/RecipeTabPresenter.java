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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The class contains business logic which allows update a recipe for current machine. The class is a tab presenter and
 * shows current machine recipe.
 *
 * @author Valeriy Svydenko
 */
public class RecipeTabPresenter implements TabPresenter {

    private final RecipeView view;

    @Inject
    public RecipeTabPresenter(RecipeView view) {
        this.view = view;
    }

    /**
     * Calls special method on view which updates recipe of current machine.
     *
     * @param machine
     *         machine for which need update information
     */
    public void updateInfo(@NotNull Machine machine) {
        String scriptLocation = machine.getRecipeLocation();
        if (!isNullOrEmpty(scriptLocation)) {
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, scriptLocation);
            try {
                requestBuilder.sendRequest(null, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        view.setScript(response.getText());
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {

                    }
                });
            } catch (RequestException exception) {
                Log.error(getClass(), exception);
            }
        } else if (!isNullOrEmpty(machine.getRecipeContent())) {
            view.setScript(machine.getRecipeContent());
        }
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
