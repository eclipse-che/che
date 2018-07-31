/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.factory.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Author;

/** Data object for {@link Author}. */
public class AuthorImpl implements Author {

  private Long created;
  private String userId;

  public AuthorImpl(String userId, Long created) {
    this.created = created;
    this.userId = userId;
  }

  public AuthorImpl(Author creator) {
    this(creator.getUserId(), creator.getCreated());
  }

  @Override
  public Long getCreated() {
    return created;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof AuthorImpl)) return false;
    final AuthorImpl other = (AuthorImpl) obj;
    return Objects.equals(userId, other.userId) && Objects.equals(created, other.created);
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 31 * result + Objects.hashCode(userId);
    result = 31 * result + Objects.hashCode(created);
    return result;
  }

  @Override
  public String toString() {
    return "AuthorImpl{" + "created=" + created + ", userId='" + userId + '\'' + '}';
  }
}
