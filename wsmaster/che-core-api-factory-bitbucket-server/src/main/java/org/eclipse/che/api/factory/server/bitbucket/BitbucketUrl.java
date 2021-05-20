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
package org.eclipse.che.api.factory.server.bitbucket;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;

/** Representation of a bitbucket URL, allowing to get details from it. */
public class BitbucketUrl implements RemoteFactoryUrl {

  /** Hostname of bitbucket URL */
  private String hostName;

  /** Project part of bitbucket URL */
  private String project;

  /** Repository part of the URL. */
  private String repository;

  /** Branch name */
  private String branch;

  /** Devfile filenames list */
  private final List<String> devfileFilenames = new ArrayList<>();

  /**
   * Creation of this instance is made by the parser so user may not need to create a new instance
   * directly
   */
  protected BitbucketUrl() {}

  /**
   * Gets hostname of this bitbucket url
   *
   * @return the project part
   */
  public String getHostName() {
    return this.hostName;
  }

  public BitbucketUrl withHostName(String hostName) {
    this.hostName = hostName;
    return this;
  }

  /**
   * Gets project of this bitbucket url
   *
   * @return the project part
   */
  public String getProject() {
    return this.project;
  }

  public BitbucketUrl withProject(String project) {
    this.project = project;
    return this;
  }

  /**
   * Gets repository of this bitbucket url
   *
   * @return the repository part
   */
  public String getRepository() {
    return this.repository;
  }

  protected BitbucketUrl withRepository(String repository) {
    this.repository = repository;
    return this;
  }

  protected BitbucketUrl withDevfileFilenames(List<String> devfileFilenames) {
    this.devfileFilenames.addAll(devfileFilenames);
    return this;
  }

  /**
   * Gets branch of this bitbucket url
   *
   * @return the branch part
   */
  public String getBranch() {
    return this.branch;
  }

  protected BitbucketUrl withBranch(String branch) {
    if (!Strings.isNullOrEmpty(branch)) {
      this.branch = branch;
    }
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
    StringJoiner joiner =
        new StringJoiner("/")
            .add(hostName)
            .add("rest/api/1.0/projects")
            .add(project)
            .add("repos")
            .add(repository)
            .add("raw")
            .add(fileName);
    String resultUrl = joiner.toString();
    if (branch != null) {
      resultUrl = resultUrl + "?at=" + branch;
    }
    return resultUrl;
  }

  /**
   * Provides location to the repository part of the full bitbucket URL.
   *
   * @return location of the repository.
   */
  protected String repositoryLocation() {
    return hostName + "/scm/" + this.project + "/" + this.repository + ".git";
  }
}
