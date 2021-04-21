/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.permission.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;

/**
 * Implementation of {@link PermissionChecker} that load permissions by http requests to {@link
 * PermissionsService}
 *
 * <p>It also caches permissions to avoid frequently requests to workspace master.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class HttpPermissionCheckerImpl implements PermissionChecker {
  private final LoadingCache<Key, Set<String>> permissionsCache;

  @Inject
  public HttpPermissionCheckerImpl(
      @Named("che.api") String apiEndpoint, HttpJsonRequestFactory requestFactory) {
    // TODO mb make configurable size of cache and expiration time
    this.permissionsCache =
        CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                new CacheLoader<Key, Set<String>>() {
                  @Override
                  public Set<String> load(Key key) throws Exception {
                    UriBuilder currentUsersPermissions =
                        UriBuilder.fromUri(apiEndpoint).path("permissions/" + key.domain);
                    if (key.instance != null) {
                      currentUsersPermissions.queryParam("instance", key.instance);
                    }
                    String userPermissionsUrl = currentUsersPermissions.build().toString();
                    try {
                      PermissionsDto usersPermissions =
                          requestFactory
                              .fromUrl(userPermissionsUrl)
                              .useGetMethod()
                              .request()
                              .asDto(PermissionsDto.class);
                      return new HashSet<>(usersPermissions.getActions());
                    } catch (NotFoundException e) {
                      // user doesn't have permissions
                      return new HashSet<>();
                    }
                  }
                });
  }

  @Override
  public boolean hasPermission(String user, String domain, String instance, String action)
      throws ServerException {
    try {
      return permissionsCache.get(new Key(user, domain, instance)).contains(action);
    } catch (Exception e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  private static final class Key {
    private final String user;
    private final String domain;
    private final String instance;

    private Key(String user, String domain, String instance) {
      this.user = user;
      this.domain = domain;
      this.instance = instance;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Key)) {
        return false;
      }
      final Key other = (Key) obj;
      return Objects.equals(user, other.user)
          && Objects.equals(domain, other.domain)
          && Objects.equals(instance, other.instance);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = hash * 31 + Objects.hashCode(user);
      hash = hash * 31 + Objects.hashCode(domain);
      hash = hash * 31 + Objects.hashCode(instance);
      return hash;
    }
  }
}
