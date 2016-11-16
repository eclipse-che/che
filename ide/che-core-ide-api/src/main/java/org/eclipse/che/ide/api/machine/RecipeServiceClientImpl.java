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
package org.eclipse.che.ide.api.machine;

import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation for {@link RecipeServiceClient}.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 * @author Sergii Leschenko
 */
public class RecipeServiceClientImpl implements RecipeServiceClient {
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderFactory          loaderFactory;
    private final String                 baseHttpUrl;

    @Inject
    protected RecipeServiceClientImpl(@RestContext String restContext,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      AsyncRequestFactory asyncRequestFactory,
                                      LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = restContext + "/recipe";
    }

    @Override
    public Promise<RecipeDescriptor> createRecipe(@NotNull final NewRecipe newRecipe) {
        return asyncRequestFactory.createPostRequest(baseHttpUrl, newRecipe)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Creating recipe..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class));
    }

    @Override
    public Promise<String> getRecipeScript(@NotNull final String id) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + '/' + id + "/script")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting recipe script..."))
                                  .send(new StringUnmarshaller());
    }

    @Override
    public Promise<RecipeDescriptor> getRecipe(@NotNull final String id) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + '/' + id)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting recipe..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class));
    }

    @Override
    public Promise<List<RecipeDescriptor>> getAllRecipes() {
        return searchRecipes(Collections.<String>emptyList(), null, 0, 0);
    }

    @Override
    public Promise<List<RecipeDescriptor>> searchRecipes(@NotNull final List<String> tags,
                                                         @Nullable final String type,
                                                         final int skipCount,
                                                         final int maxItems) {
        final StringBuilder tagsParam = new StringBuilder();
        for (String tag : tags) {
            tagsParam.append("&tags=").append(tag);
        }

        String url = baseHttpUrl + "?skipCount=" + skipCount +
                     "&maxItems=" + maxItems +
                     tagsParam.toString();

        if (type != null) {
            url += "&type=" + type;
        }

        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Searching recipes..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(RecipeDescriptor.class));
    }

    @Override
    public Promise<RecipeDescriptor> updateRecipe(@NotNull final RecipeUpdate recipeUpdate) {
        return asyncRequestFactory.createRequest(PUT, baseHttpUrl, recipeUpdate, false)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Updating recipe..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class));
    }

    @Override
    public Promise<Void> removeRecipe(@NotNull final String id) {
        return asyncRequestFactory.createRequest(DELETE, baseHttpUrl + '/' + id, null, false)
                                  .loader(loaderFactory.newLoader("Deleting recipe..."))
                                  .send();
    }
}
