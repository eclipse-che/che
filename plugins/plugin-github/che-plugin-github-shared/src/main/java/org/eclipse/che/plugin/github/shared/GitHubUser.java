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
package org.eclipse.che.plugin.github.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: GitHubUser.java Aug 6, 2012
 */
@DTO
public interface GitHubUser {
  /** @return the type */
  String getType();

  void setType(String type);

  /** @return the email */
  String getEmail();

  void setEmail(String email);

  /** @return the company */
  String getCompany();

  void setCompany(String company);

  /** @return the followers */
  int getFollowers();

  void setFollowers(int followers);

  /** @return the avatar_url */
  String getAvatarUrl();

  void setAvatarUrl(String avatarUrl);

  /** @return the html_url */
  String getHtmlUrl();

  void setHtmlUrl(String htmlUrl);

  /** @return the bio */
  String getBio();

  void setBio(String bio);

  /** @return the public_repos */
  int getPublicRepos();

  void setPublicRepos(int publicRepos);

  /** @return the public_gists */
  int getPublicGists();

  void setPublicGists(int publicGists);

  /** @return the following */
  int getFollowing();

  void setFollowing(int following);

  /** @return the location */
  String getLocation();

  void setLocation(String location);

  /** @return the name */
  String getName();

  void setName(String name);

  /** @return the url */
  String getUrl();

  void setUrl(String url);

  /** @return the gravatar_id */
  String getGravatarId();

  void setGravatarId(String gravatarId);

  /** @return the id */
  String getId();

  void setId(String id);

  /** @return the login */
  String getLogin();

  void setLogin(String login);
}
