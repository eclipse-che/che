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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.StringUnmarshaller;

import javax.validation.constraints.NotNull;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * Implementation for {@link RecipeServiceClient}.
 *
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
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

    /** {@inheritDoc} */
    @Override
    public Promise<RecipeDescriptor> createRecipe(@NotNull final NewRecipe newRecipe) {
        return newPromise(new RequestCall<RecipeDescriptor>() {
            @Override
            public void makeCall(AsyncCallback<RecipeDescriptor> callback) {
                createRecipe(newRecipe, callback);
            }
        });
    }

    private void createRecipe(@NotNull final NewRecipe newRecipe, @NotNull AsyncCallback<RecipeDescriptor> callback) {
        asyncRequestFactory.createPostRequest(baseHttpUrl, newRecipe)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Creating recipe..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getRecipeScript(@NotNull final String id) {
        return newPromise(new RequestCall<String>() {
            @Override
            public void makeCall(AsyncCallback<String> callback) {
                getRecipeScript(id, callback);
            }
        });
    }

    private void getRecipeScript(@NotNull String id, @NotNull AsyncCallback<String> callback) {
        final String url = baseHttpUrl + '/' + id + "/script";
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting recipe script..."))
                           .send(newCallback(callback, new StringUnmarshaller()));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RecipeDescriptor> getRecipe(@NotNull final String id) {
        return newPromise(new RequestCall<RecipeDescriptor>() {
            @Override
            public void makeCall(AsyncCallback<RecipeDescriptor> callback) {
                getRecipe(id, callback);
            }
        });
    }

    private void getRecipe(@NotNull String id, @NotNull AsyncCallback<RecipeDescriptor> callback) {
        final String url = baseHttpUrl + '/' + id;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting recipe..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<RecipeDescriptor>> getAllRecipes() {
        return newPromise(new RequestCall<List<RecipeDescriptor>>() {
            @Override
            public void makeCall(AsyncCallback<List<RecipeDescriptor>> callback) {
                getRecipes(0, -1, callback);
            }
        }).then(new Function<List<RecipeDescriptor>, List<RecipeDescriptor>>() {
            @Override
            public List<RecipeDescriptor> apply(List<RecipeDescriptor> arg) throws FunctionException {
                final ArrayList<RecipeDescriptor> descriptors = new ArrayList<>();
                for (RecipeDescriptor descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<RecipeDescriptor>> getRecipes(final int skipCount, final int maxItems) {
        return newPromise(new RequestCall<List<RecipeDescriptor>>() {
            @Override
            public void makeCall(AsyncCallback<List<RecipeDescriptor>> callback) {
                getRecipes(skipCount, maxItems, callback);
            }
        }).then(new Function<List<RecipeDescriptor>, List<RecipeDescriptor>>() {
            @Override
            public List<RecipeDescriptor> apply(List<RecipeDescriptor> arg) throws FunctionException {
                final ArrayList<RecipeDescriptor> descriptors = new ArrayList<>();
                for (RecipeDescriptor descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    private void getRecipes(int skipCount, int maxItems, @NotNull AsyncCallback<List<RecipeDescriptor>> callback) {
        String url = baseHttpUrl + "/list?skipCount=" + skipCount;
        if (maxItems > 0) {
            url += "&maxItems=" + maxItems;
        }

        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting recipes..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(RecipeDescriptor.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<List<RecipeDescriptor>> searchRecipes(@NotNull final List<String> tags,
                                                         @Nullable final String type,
                                                         final int skipCount,
                                                         final int maxItems) {
        return newPromise(new RequestCall<List<RecipeDescriptor>>() {
            @Override
            public void makeCall(AsyncCallback<List<RecipeDescriptor>> callback) {
                searchRecipes(tags, type, skipCount, maxItems, callback);
            }
        }).then(new Function<List<RecipeDescriptor>, List<RecipeDescriptor>>() {
            @Override
            public List<RecipeDescriptor> apply(List<RecipeDescriptor> arg) throws FunctionException {
                final ArrayList<RecipeDescriptor> descriptors = new ArrayList<>();
                for (RecipeDescriptor descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    private void searchRecipes(@NotNull List<String> tags,
                               @Nullable String type,
                               int skipCount,
                               int maxItems,
                               @NotNull AsyncCallback<List<RecipeDescriptor>> callback) {
        final StringBuilder tagsParam = new StringBuilder();
        for (String tag : tags) {
            tagsParam.append("tags=").append(tag).append("&");
        }
        if (tagsParam.length() > 0) {
            tagsParam.deleteCharAt(tagsParam.length() - 1); // delete last ampersand
        }

        final String url = baseHttpUrl + "/list?" + tagsParam.toString() +
                           (tagsParam.length() > 0 ? '&' : "") +
                           "type=" + type +
                           "&skipCount=" + skipCount +
                           "&maxItems=" + maxItems;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Searching recipes..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(RecipeDescriptor.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<RecipeDescriptor> updateRecipe(@NotNull final RecipeUpdate recipeUpdate) {
        return newPromise(new RequestCall<RecipeDescriptor>() {
            @Override
            public void makeCall(AsyncCallback<RecipeDescriptor> callback) {
                updateCommand(recipeUpdate, callback);
            }
        });
    }

    private void updateCommand(@NotNull RecipeUpdate recipeUpdate, @NotNull AsyncCallback<RecipeDescriptor> callback) {
        final String url = baseHttpUrl;
        asyncRequestFactory.createRequest(PUT, url, recipeUpdate, false)
                           .header(ACCEPT, APPLICATION_JSON)
                           .header(CONTENT_TYPE, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Updating recipe..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(RecipeDescriptor.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Promise<Void> removeRecipe(@NotNull final String id) {
        return newPromise(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                removeRecipe(id, callback);
            }
        });
    }

    private void removeRecipe(@NotNull String id, @NotNull AsyncCallback<Void> callback) {
        asyncRequestFactory.createRequest(DELETE, baseHttpUrl + '/' + id, null, false)
                           .loader(loaderFactory.newLoader("Deleting recipe..."))
                           .send(newCallback(callback));
    }
}
