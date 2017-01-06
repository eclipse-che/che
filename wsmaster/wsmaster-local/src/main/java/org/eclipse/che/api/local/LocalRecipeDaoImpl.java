/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * In-memory implementation of {@link RecipeDao}.
 *
 * <p>The implementation is thread-safe guarded by this instance.
 * Clients may use instance locking to perform extra, thread-safe operation.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Singleton
public class LocalRecipeDaoImpl implements RecipeDao {

    public static final String FILENAME = "recipes.json";

    @VisibleForTesting
    final Map<String, RecipeImpl> recipes;

    private final LocalStorage recipeStorage;

    @Inject
    public LocalRecipeDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        this.recipeStorage = storageFactory.create(FILENAME);
        this.recipes = new HashMap<>();
    }

    @PostConstruct
    public synchronized void loadRecipes() {
        recipes.putAll(recipeStorage.loadMap(new TypeToken<Map<String, RecipeImpl>>() {}));
    }

    public synchronized void saveRecipes() throws IOException {
        recipeStorage.store(recipes);
    }

    @Override
    public synchronized void create(RecipeImpl recipe) throws ConflictException {
        if (recipes.containsKey(recipe.getId())) {
            throw new ConflictException(format("Recipe with id %s already exists", recipe.getId()));
        }
        recipes.put(recipe.getId(), recipe);
    }

    @Override
    public synchronized RecipeImpl update(RecipeImpl update) throws NotFoundException {
        final RecipeImpl target = recipes.get(update.getId());
        if (target == null) {
            throw new NotFoundException(format("Recipe with id '%s' was not found", update.getId()));
        }
        if (update.getType() != null) {
            target.setType(update.getType());
        }
        if (update.getScript() != null) {
            target.setScript(update.getScript());
        }
        if (update.getCreator() != null) {
            target.setCreator(update.getCreator());
        }
        if (update.getDescription() != null) {
            target.setDescription(update.getDescription());
        }
        if (update.getName() != null) {
            target.setName(update.getName());
        }
        if (!update.getTags().isEmpty()) {
            target.setTags(update.getTags());
        }

        return new RecipeImpl(target);
    }

    @Override
    public synchronized void remove(String id) {
        requireNonNull(id);
        recipes.remove(id);
    }

    @Override
    public synchronized RecipeImpl getById(String id) throws NotFoundException {
        requireNonNull(id);
        final RecipeImpl recipe = recipes.get(id);
        if (recipe == null) {
            throw new NotFoundException(format("Recipe with id %s was not found", id));
        }
        return new RecipeImpl(recipe);
    }

    @Override
    public synchronized List<RecipeImpl> search(String user, List<String> tags, String type, int skipCount, int maxItems)
            throws ServerException {
        Stream<RecipeImpl> recipesStream = recipes.values()
                                                  .stream()
                                                  .filter(recipe -> (tags == null || recipe.getTags().containsAll(tags))
                                                                    && (type == null || type.equals(recipe.getType())))
                                                  .skip(skipCount);
        if (maxItems != 0) {
            recipesStream = recipesStream.limit(maxItems);
        }
        return recipesStream.collect(Collectors.toList());
    }
}
