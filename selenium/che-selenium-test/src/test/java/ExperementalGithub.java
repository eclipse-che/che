/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExperementalGithub {

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  private GitHub gitHub;
  private GHRepository ghRepository;

  private final String NAME_REPO =
      NameGenerator.generate(ExperementalGithub.class.getSimpleName(), 3);

  @BeforeClass
  public void setUp() throws IOException {
    this.gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    ghRepository = gitHub.createRepository(NAME_REPO).create();
  }

  @Test
  public void checkConnection() throws IOException {
    Path entryPath =
        Paths.get(getClass().getResource("/projects/depended-on-git/gitPullTest").getPath());
    List<Pair<Path, Path>> projectEntries =
        Files.walk(entryPath)
            .filter(Files::isRegularFile)
            .map(
                path ->
                    Pair.of(
                        path,
                        Paths.get(
                                getClass()
                                    .getResource("/projects/depended-on-git/gitPullTest")
                                    .getPath())
                            .relativize(path)))
            .collect(Collectors.toList());
    for (Pair<Path, Path> projectEntry : projectEntries) {
      ghRepository.createContent(
          Files.readAllBytes(projectEntry.first),
          "add new content",
          projectEntry.second.toString());
    }

    ghRepository.delete();
  }
}
