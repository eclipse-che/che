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
package org.eclipse.che.api.factory.server.github;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.eclipse.che.api.factory.server.urlfactory.ProjectConfigDtoMerger;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Validate operations performed by the Github Factory service
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GithubFactoryParametersResolverTest {

  /** Parser which will allow to check validity of URLs and create objects. */
  @Spy private GithubURLParser githubUrlParser = new GithubURLParser();

  /** Converter allowing to convert github URL to other objects. */
  @Spy
  private GithubSourceStorageBuilder githubSourceStorageBuilder = new GithubSourceStorageBuilder();

  /** ProjectDtoMerger */
  @Mock private ProjectConfigDtoMerger projectConfigDtoMerger = new ProjectConfigDtoMerger();

  /** Parser which will allow to check validity of URLs and create objects. */
  @Mock private URLFactoryBuilder urlFactoryBuilder;

  /** Capturing the project config DTO supplier parameter. */
  @Captor private ArgumentCaptor<Supplier<ProjectConfigDto>> projectConfigDtoArgumentCaptor;

  /**
   * Capturing the location parameter when calling {@link
   * URLFactoryBuilder#createFactoryFromJson(RemoteFactoryUrl)} or {@link
   * URLFactoryBuilder#createFactoryFromDevfile(RemoteFactoryUrl, FileContentProvider)}
   */
  @Captor private ArgumentCaptor<RemoteFactoryUrl> factoryUrlArgumentCaptor;

  /** Instance of resolver that will be tested. */
  @InjectMocks private GithubFactoryParametersResolver githubFactoryParametersResolver;

  /** Check missing parameter name can't be accepted by this resolver */
  @Test
  public void checkMissingParameter() {
    Map<String, String> parameters = singletonMap("foo", "this is a foo bar");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertFalse(accept);
  }

  /** Check url which is not a github url can't be accepted by this resolver */
  @Test
  public void checkInvalidAcceptUrl() {
    Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, "http://www.eclipse.org/che");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertFalse(accept);
  }

  /** Check github url will be be accepted by this resolver */
  @Test
  public void checkValidAcceptUrl() {
    Map<String, String> parameters =
        singletonMap(URL_PARAMETER_NAME, "https://github.com/codenvy/codenvy.git");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertTrue(accept);
  }

  /** Check that with a simple valid URL github url it works */
  @Test
  public void shouldReturnGitHubSimpleRepoFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV(CURRENT_VERSION).withSource("repo");
    when(urlFactoryBuilder.createFactoryFromJson(any(RemoteFactoryUrl.class)))
        .thenReturn(Optional.empty());
    when(urlFactoryBuilder.createFactoryFromDevfile(any(RemoteFactoryUrl.class), any()))
        .thenReturn(Optional.empty());

    when(projectConfigDtoMerger.merge(any(FactoryDto.class), any())).then(returnsFirstArg());

    FactoryDto factory =
        githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder).buildDefaultWorkspaceConfig(eq("che"));
    assertEquals(factory, computedFactory);
  }

  /** Check that with a simple valid URL github url it works */
  @Test
  public void shouldReturnGitHubSimpleJsonFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV(CURRENT_VERSION);
    when(urlFactoryBuilder.createFactoryFromJson(any(RemoteFactoryUrl.class)))
        .thenReturn(Optional.of(computedFactory));

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following factory json file
    verify(urlFactoryBuilder).createFactoryFromJson(factoryUrlArgumentCaptor.capture());
    assertEquals(
        factoryUrlArgumentCaptor.getValue().factoryFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/.factory.json");

    assertEquals(factoryUrlArgumentCaptor.getValue().getFactoryFilename(), ".factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder).buildDefaultWorkspaceConfig(eq("che"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue().get();

    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "github");
    assertEquals(sourceStorageDto.getLocation(), githubUrl);
    Map<String, String> sourceParameters = sourceStorageDto.getParameters();
    assertEquals(sourceParameters.size(), 1);
    assertEquals(sourceParameters.get("branch"), "master");
  }

  /** Check that with a simple valid URL github url it works */
  @Test
  public void shouldReturnGitHubDevfileFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV(CURRENT_VERSION);
    when(urlFactoryBuilder.createFactoryFromDevfile(any(RemoteFactoryUrl.class), any()))
        .thenReturn(Optional.of(computedFactory));

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following devfile
    verify(urlFactoryBuilder).createFactoryFromDevfile(factoryUrlArgumentCaptor.capture(), any());
    assertEquals(
        factoryUrlArgumentCaptor.getValue().devfileFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/master/devfile.yaml");

    assertEquals(factoryUrlArgumentCaptor.getValue().getDevfileFilename(), "devfile.yaml");
    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue().get();

    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "github");
    assertEquals(sourceStorageDto.getLocation(), githubUrl);
    Map<String, String> sourceParameters = sourceStorageDto.getParameters();
    assertEquals(sourceParameters.size(), 1);
    assertEquals(sourceParameters.get("branch"), "master");
  }

  /** Check that we've expected branch when url contains a branch name */
  @Test
  public void shouldReturnGitHubBranchFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che/tree/4.2.x";
    String githubCloneUrl = "https://github.com/eclipse/che";
    String githubBranch = "4.2.x";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV(CURRENT_VERSION);
    when(urlFactoryBuilder.createFactoryFromJson(any(RemoteFactoryUrl.class)))
        .thenReturn(Optional.of(computedFactory));

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following factory json file
    verify(urlFactoryBuilder).createFactoryFromJson(factoryUrlArgumentCaptor.capture());
    assertEquals(
        factoryUrlArgumentCaptor.getValue().factoryFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.json");

    assertEquals(factoryUrlArgumentCaptor.getValue().getFactoryFilename(), ".factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder).buildDefaultWorkspaceConfig(eq("che"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue().get();
    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "github");
    assertEquals(sourceStorageDto.getLocation(), githubCloneUrl);
    Map<String, String> sourceParameters = sourceStorageDto.getParameters();
    assertEquals(sourceParameters.size(), 1);
    assertEquals(sourceParameters.get("branch"), githubBranch);
  }

  /** Check that we have a sparse checkout "keepDir" if url contains branch and subtree. */
  @Test
  public void shouldReturnGitHubBranchAndKeepdirFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che/tree/4.2.x/dashboard";
    String githubCloneUrl = "https://github.com/eclipse/che";
    String githubBranch = "4.2.x";
    String githubKeepdir = "dashboard";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV(CURRENT_VERSION);
    when(urlFactoryBuilder.createFactoryFromJson(any(RemoteFactoryUrl.class)))
        .thenReturn(Optional.of(computedFactory));

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following factory json file
    verify(urlFactoryBuilder).createFactoryFromJson(factoryUrlArgumentCaptor.capture());
    assertEquals(
        factoryUrlArgumentCaptor.getValue().factoryFileLocation(),
        "https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.json");

    assertEquals(factoryUrlArgumentCaptor.getValue().getFactoryFilename(), ".factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder).buildDefaultWorkspaceConfig(eq("che"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue().get();
    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "github");
    assertEquals(sourceStorageDto.getLocation(), githubCloneUrl);
    Map<String, String> sourceParameters = sourceStorageDto.getParameters();
    assertEquals(sourceParameters.size(), 2);
    assertEquals(sourceParameters.get("branch"), githubBranch);
    assertEquals(sourceParameters.get("keepDir"), githubKeepdir);
  }
}
