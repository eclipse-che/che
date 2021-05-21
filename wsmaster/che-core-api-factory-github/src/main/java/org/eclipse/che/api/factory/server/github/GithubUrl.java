/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.github;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;

/**
 * Representation of a github URL, allowing to get details from it.
 *
 * <p>like https://github.com/<username>/<repository>
 * https://github.com/<username>/<repository>/tree/<branch>
 *
 * @author Florent Benoit
 */
public class GithubUrl implements RemoteFactoryUrl {

  private final String NAME = "github";

  private static final String HOSTNAME = "https://github.com";

  /** Username part of github URL */
  private String username;

  /** Repository part of the URL. */
  private String repository;

  /** Branch name */
  private String branch;

  /** Subfolder if any */
  private String subfolder;

  /** Devfile filenames list */
  private final List<String> devfileFilenames = new ArrayList<>();

  /**
   * Creation of this instance is made by the parser so user may not need to create a new instance
   * directly
   */
  protected GithubUrl() {}

  @Override
  public String getProviderName() {
    return NAME;
  }

  /**
   * Gets username of this github url
   *
   * @return the username part
   */
  public String getUsername() {
    return this.username;
  }

  public GithubUrl withUsername(String userName) {
    this.username = userName;
    return this;
  }

  /**
   * Gets repository of this github url
   *
   * @return the repository part
   */
  public String getRepository() {
    return this.repository;
  }

  protected GithubUrl withRepository(String repository) {
    this.repository = repository;
    return this;
  }

  protected GithubUrl withDevfileFilenames(List<String> devfileFilenames) {
    this.devfileFilenames.addAll(devfileFilenames);
    return this;
  }

  /**
   * Gets branch of this github url
   *
   * @return the branch part
   */
  public String getBranch() {
    return this.branch;
  }

  protected GithubUrl withBranch(String branch) {
    if (!Strings.isNullOrEmpty(branch)) {
      this.branch = branch;
    }
    return this;
  }

  /**
   * Gets subfolder of this github url
   *
   * @return the subfolder part
   */
  public String getSubfolder() {
    return this.subfolder;
  }

  /**
   * Sets the subfolder represented by the URL.
   *
   * @param subfolder path inside the repository
   * @return current github instance
   */
  protected GithubUrl withSubfolder(String subfolder) {
    this.subfolder = subfolder;
    return this;
  }

  /**
   * Provides list of configured devfile filenames with locations
   *
   * @return list of devfile filenames and locations
   */
  @Override
  public List<DevfileLocation> devfileFileLocations() {
    return devfileFilenames.stream().map(this::createDevfileLocation).collect(Collectors.toList());
  }

  private DevfileLocation createDevfileLocation(String devfileFilename) {
    return new DevfileLocation() {
      @Override
      public Optional<String> filename() {
        return Optional.of(devfileFilename);
      }

      @Override
      public String location() {
        return rawFileLocation(devfileFilename);
      }
    };
  }

  /**
   * Provides location to raw content of specified file
   *
   * @return location of specified file in a repository
   */
  public String rawFileLocation(String fileName) {
    return new StringJoiner("/")
        .add("https://raw.githubusercontent.com")
        .add(username)
        .add(repository)
        .add(firstNonNull(branch, "HEAD"))
        .add(fileName)
        .toString();
  }

  @Override
  public String getHostName() {
    return HOSTNAME;
  }

  /**
   * Provides location to the repository part of the full github URL.
   *
   * @return location of the repository.
   */
  protected String repositoryLocation() {
    return HOSTNAME + "/" + this.username + "/" + this.repository + ".git";
  }
}
