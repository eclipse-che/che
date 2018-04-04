/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.jpa;

import static java.util.Objects.requireNonNull;

import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.spi.PreferenceDao;

/**
 * Implementation of {@link PreferenceDao}.
 *
 * @author Anton Korneta
 */
@Singleton
public class JpaPreferenceDao implements PreferenceDao {

  @Inject private Provider<EntityManager> managerProvider;

  @Override
  public void setPreferences(String userId, Map<String, String> preferences)
      throws ServerException {
    requireNonNull(userId);
    requireNonNull(preferences);
    final PreferenceEntity prefs = new PreferenceEntity(userId, preferences);
    if (preferences.isEmpty()) {
      remove(userId);
    } else {
      try {
        doSetPreference(prefs);
      } catch (RuntimeException ex) {
        throw new ServerException(ex.getLocalizedMessage(), ex);
      }
    }
  }

  @Override
  @Transactional
  public Map<String, String> getPreferences(String userId) throws ServerException {
    requireNonNull(userId);
    try {
      final EntityManager manager = managerProvider.get();
      final PreferenceEntity prefs = manager.find(PreferenceEntity.class, userId);
      return prefs == null ? new HashMap<>() : prefs.getPreferences();
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  @Transactional
  public Map<String, String> getPreferences(String userId, String filter) throws ServerException {
    requireNonNull(userId);
    requireNonNull(filter);
    try {
      final EntityManager manager = managerProvider.get();
      final PreferenceEntity prefs = manager.find(PreferenceEntity.class, userId);
      if (prefs == null) {
        return new HashMap<>();
      }
      final Map<String, String> preferences = prefs.getPreferences();
      if (!filter.isEmpty()) {
        final Pattern pattern = Pattern.compile(filter);
        return preferences
            .entrySet()
            .stream()
            .filter(preference -> pattern.matcher(preference.getKey()).matches())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      } else {
        return preferences;
      }
    } catch (RuntimeException ex) {
      throw new ServerException(ex.getLocalizedMessage(), ex);
    }
  }

  @Override
  public void remove(String userId) throws ServerException {
    requireNonNull(userId);
    try {
      doRemove(userId);
    } catch (RuntimeException ex) {
      throw new ServerException(ex);
    }
  }

  @Transactional
  protected void doSetPreference(PreferenceEntity prefs) {
    final EntityManager manager = managerProvider.get();
    final PreferenceEntity existing = manager.find(PreferenceEntity.class, prefs.getUserId());
    if (existing != null) {
      manager.merge(prefs);
    } else {
      manager.persist(prefs);
    }
    manager.flush();
  }

  @Transactional
  protected void doRemove(String userId) {
    final EntityManager manager = managerProvider.get();
    final PreferenceEntity prefs = manager.find(PreferenceEntity.class, userId);
    if (prefs != null) {
      manager.remove(prefs);
      manager.flush();
    }
  }
}
