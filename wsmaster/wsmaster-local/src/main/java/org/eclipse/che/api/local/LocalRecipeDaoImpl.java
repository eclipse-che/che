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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author Eugene Voevodin
 * @author Anton Korneta
 */
@Singleton
public class LocalRecipeDaoImpl implements RecipeDao {

    @VisibleForTesting
    final Map<String, RecipeImpl> recipes;

    private final ReadWriteLock lock;
    private final LocalStorage  recipeStorage;

    @Inject
    public LocalRecipeDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        this.recipeStorage = storageFactory.create("recipes.json");
        this.recipes = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @PostConstruct
    public void loadRecipes() {
        recipes.putAll(recipeStorage.loadMap(new TypeToken<Map<String, RecipeImpl>>() {}));
    }

    @PreDestroy
    public void saveRecipes() throws IOException {
        recipeStorage.store(recipes);
    }

    @Override
    public void create(RecipeImpl recipe) throws ConflictException {
        lock.writeLock().lock();
        try {
            if (recipes.containsKey(recipe.getId())) {
                throw new ConflictException(format("Recipe with id %s already exists", recipe.getId()));
            }
            recipes.put(recipe.getId(), recipe);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RecipeImpl update(RecipeImpl update) throws NotFoundException {
        lock.writeLock().lock();
        try {
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
            if (update.getAcl() != null && !update.getAcl().isEmpty()) {
                target.setAcl(update.getAcl());
            }

            return new RecipeImpl(target);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(String id) {
        requireNonNull(id);
        lock.writeLock().lock();
        try {
            recipes.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RecipeImpl getById(String id) throws NotFoundException {
        requireNonNull(id);
        lock.readLock().lock();
        try {
            final RecipeImpl recipe = recipes.get(id);
            if (recipe == null) {
                throw new NotFoundException(format("Recipe with id %s was not found", id));
            }
            return new RecipeImpl(recipe);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RecipeImpl> search(String user, List<String> tags, String type, int skipCount, int maxItems) throws ServerException {
        lock.readLock().lock();
        try {
            Stream<RecipeImpl> recipesStream = recipes.values()
                                                      .stream()
                                                      .filter(recipe -> (tags == null || recipe.getTags().containsAll(tags))
                                                                        && (type == null || type.equals(recipe.getType())))
                                                      .skip(skipCount);
            if (maxItems != 0) {
                recipesStream = recipesStream.limit(maxItems);
            }
            return recipesStream.collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}
