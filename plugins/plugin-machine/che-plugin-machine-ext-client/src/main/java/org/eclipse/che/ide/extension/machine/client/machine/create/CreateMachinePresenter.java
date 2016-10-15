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
package org.eclipse.che.ide.extension.machine.client.machine.create;

import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;

import java.util.Collections;
import java.util.List;

/**
 * Presenter for creating machine.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CreateMachinePresenter implements CreateMachineView.ActionDelegate {

    private static final String URL_PATTERN =
            "(https?|ftp)://(www\\.)?(((([a-zA-Z0-9.-]+\\.){1,}[a-zA-Z]{2,4}|localhost))|((\\d{1,3}\\.){3}(\\d{1,3})))(:(\\d+))?(/" +
            "([a-zA-Z0-9-._~!$&'()*+,;=:@/]|%[0-9A-F]{2})*)?(\\?([a-zA-Z0-9-._~!$&'()*+,;=:/?@]|%[0-9A-F]{2})*)?(#([a-zA-Z0-9" +
            "._-]|%[0-9A-F]{2})*)?";
    private static final RegExp URL         = RegExp.compile(URL_PATTERN);

    private static final String RECIPE_TYPE = "docker";
    private static final int    SKIP_COUNT  = 0;
    private static final int    MAX_COUNT   = 100;

    private final CreateMachineView   view;
    private final AppContext          appContext;
    private final MachineManager      machineManager;
    private final RecipeServiceClient recipeServiceClient;

    @Inject
    public CreateMachinePresenter(CreateMachineView view,
                                  AppContext appContext,
                                  MachineManager machineManager,
                                  RecipeServiceClient recipeServiceClient) {
        this.view = view;
        this.appContext = appContext;
        this.machineManager = machineManager;
        this.recipeServiceClient = recipeServiceClient;

        view.setDelegate(this);
    }

    public void showDialog() {
        view.show();

        view.setCreateButtonState(false);
        view.setReplaceButtonState(false);
        view.setMachineName("");
        view.setErrorHint(false);
        view.setNoRecipeHint(false);
        view.setTags("");
        view.setRecipeURL("");
    }

    @Override
    public void onNameChanged() {
        checkButtons();
    }

    @Override
    public void onRecipeUrlChanged() {
        checkButtons();
    }

    @Override
    public void onTagsChanged() {
        if (view.getTags().isEmpty()) {
            view.setRecipes(Collections.<RecipeDescriptor>emptyList());
            view.setNoRecipeHint(false);
            return;
        }

        recipeServiceClient.searchRecipes(view.getTags(), RECIPE_TYPE, SKIP_COUNT, MAX_COUNT).then(new Operation<List<RecipeDescriptor>>() {
            @Override
            public void apply(List<RecipeDescriptor> arg) throws OperationException {
                view.setRecipes(arg);
                view.setNoRecipeHint(arg.isEmpty());
            }
        });
    }

    @Override
    public void onRecipeSelected(RecipeDescriptor recipe) {
        view.setRecipeURL(recipe.getLink("get recipe script").getHref());
    }

    private void checkButtons() {
        final String recipeURL = view.getRecipeURL();
        final boolean urlValid = URL.test(recipeURL);

        view.setErrorHint(!urlValid);

        final boolean allowCreation = urlValid && !view.getMachineName().isEmpty();

        view.setCreateButtonState(allowCreation);
        view.setReplaceButtonState(allowCreation);
    }

    @Override
    public void onCreateClicked() {
        final String machineName = view.getMachineName();
        final String recipeURL = view.getRecipeURL();
        machineManager.startMachine(recipeURL, machineName);

        view.close();
    }

    @Override
    public void onReplaceDevMachineClicked() {
        final String machineName = view.getMachineName();
        final String recipeURL = view.getRecipeURL();
        final DevMachine devMachine = appContext.getDevMachine();

        if (devMachine != null) {
            machineManager.destroyMachine(devMachine);
        }

        machineManager.startDevMachine(recipeURL, machineName);
        view.close();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }
}
