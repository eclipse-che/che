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


import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.recipe.adapters.GroupAdapter;
import org.eclipse.che.api.machine.server.recipe.adapters.PermissionsAdapter;
import org.eclipse.che.api.machine.server.dao.RecipeDao;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
import org.eclipse.che.api.machine.shared.Permissions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Eugene Voevodin
 * @author Anton Korneta
 */
@Singleton
public class LocalRecipeDaoImpl implements RecipeDao {

    private final Map<String, ManagedRecipe> recipes;
    private final ReadWriteLock              lock;
    private final LocalStorage               recipeStorage;

    @Inject
    public LocalRecipeDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        Map<Class<?>, Object> adapters = ImmutableMap.of(Permissions.class, new PermissionsAdapter(), Group.class, new GroupAdapter());
        this.recipeStorage = storageFactory.create("recipes.json", adapters);
        this.recipes = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    @PostConstruct
    public void start() {
        recipes.putAll(recipeStorage.loadMap(new TypeToken<Map<String, RecipeImpl>>() {}));
    }

    @PreDestroy
    public void stop() throws IOException {
        Map<String, ManagedRecipe> recipesToStore = recipes.values()
                                                           .stream()
                                                           .filter(recipe -> !"codenvy".equals(recipe.getCreator()))
                                                           .collect(toMap(ManagedRecipe::getId, identity()));

        recipeStorage.store(recipesToStore);
    }

    @Override
    public void create(ManagedRecipe recipe) throws ConflictException {
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
    public void update(ManagedRecipe update) throws NotFoundException {
        lock.writeLock().lock();
        try {
            final RecipeImpl target = (RecipeImpl)recipes.get(update.getId());
            if (target == null) {
                throw new NotFoundException(format("Recipe with id '%s' was not found", update.getId()));
            }
            if (update.getType() != null) {
                target.setType(update.getType());
            }
            if (update.getScript() != null) {
                target.setScript(update.getScript());
            }
            if (update.getName() != null) {
                target.setName(update.getName());
            }
            if (update.getPermissions() != null) {
                target.setPermissions(update.getPermissions());
            }
            if (!update.getTags().isEmpty()) {
                target.setTags(update.getTags());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(String id) {
        lock.writeLock().lock();
        try {
            recipes.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ManagedRecipe getById(String id) throws NotFoundException {
        lock.readLock().lock();
        try {
            final ManagedRecipe recipe = recipes.get(id);
            if (recipe == null) {
                throw new NotFoundException(format("Recipe with id %s was not found", id));
            }
            return recipe;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ManagedRecipe> search(final List<String> tags, final String type, int skipCount, int maxItems) {
        lock.readLock().lock();
        try {
            return FluentIterable.from(recipes.values())
                                 .skip(skipCount)
                                 .filter(new Predicate<ManagedRecipe>() {
                                     @Override
                                     public boolean apply(ManagedRecipe recipe) {
                                         return (tags == null || recipe.getTags().containsAll(tags))
                                                && (type == null || type.equals(recipe.getType()));
                                     }
                                 })
                                 .limit(maxItems)
                                 .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ManagedRecipe> getByCreator(final String creator, int skipCount, int maxItems) {
        lock.readLock().lock();
        try {
            return FluentIterable.from(recipes.values())
                                 .skip(skipCount)
                                 .filter(new Predicate<ManagedRecipe>() {
                                     @Override
                                     public boolean apply(ManagedRecipe recipe) {
                                         return recipe.getCreator().equals(creator);
                                     }
                                 })
                                 .limit(maxItems)
                                 .toList();
        } finally {
            lock.readLock().unlock();
        }
    }
}
