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
package org.eclipse.che.plugin.github.factory.resolver;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.plugin.github.factory.resolver.GithubFactoryParametersResolver.URL_PARAMETER_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.plugin.urlfactory.ProjectConfigDtoMerger;
import org.eclipse.che.plugin.urlfactory.URLFactoryBuilder;
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
  @Spy private GithubURLParserImpl githubUrlParser = new GithubURLParserImpl();

  /** Converter allowing to convert github URL to other objects. */
  @Spy
  private GithubSourceStorageBuilder githubSourceStorageBuilder = new GithubSourceStorageBuilder();

  /** ProjectDtoMerger */
  @Mock private ProjectConfigDtoMerger projectConfigDtoMerger = new ProjectConfigDtoMerger();

  /** Parser which will allow to check validity of URLs and create objects. */
  @Mock private URLFactoryBuilder urlFactoryBuilder;

  /** Capturing the project config DTO parameter. */
  @Captor private ArgumentCaptor<ProjectConfigDto> projectConfigDtoArgumentCaptor;

  /** Capturing the parameter when calling {@link URLFactoryBuilder#createFactory(String)} */
  @Captor private ArgumentCaptor<String> jsonFileLocationArgumentCaptor;

  /** Instance of resolver that will be tested. */
  @InjectMocks private GithubFactoryParametersResolver githubFactoryParametersResolver;

  /** Check missing parameter name can't be accepted by this resolver */
  @Test
  public void checkMissingParameter() throws BadRequestException {
    Map<String, String> parameters = singletonMap("foo", "this is a foo bar");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertFalse(accept);
  }

  /** Check url which is not a github url can't be accepted by this resolver */
  @Test
  public void checkInvalidAcceptUrl() throws BadRequestException {
    Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, "http://www.eclipse.org/che");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertFalse(accept);
  }

  /** Check github url will be be accepted by this resolver */
  @Test
  public void checkValidAcceptUrl() throws BadRequestException {
    Map<String, String> parameters =
        singletonMap(URL_PARAMETER_NAME, "https://github.com/codenvy/codenvy.git");
    boolean accept = githubFactoryParametersResolver.accept(parameters);
    // shouldn't be accepted
    assertTrue(accept);
  }

  /** Check that with a simple valid URL github url it works */
  @Test
  public void shouldReturnGitHubSimpleFactory() throws Exception {

    String githubUrl = "https://github.com/eclipse/che";

    FactoryDto computedFactory = newDto(FactoryDto.class).withV("4.0");
    when(urlFactoryBuilder.createFactory(anyString())).thenReturn(computedFactory);

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following codenvy json file
    verify(urlFactoryBuilder).createFactory(jsonFileLocationArgumentCaptor.capture());
    assertEquals(
        jsonFileLocationArgumentCaptor.getValue(),
        "https://raw.githubusercontent.com/eclipse/che/master/.factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder)
        .buildWorkspaceConfig(
            eq("che"),
            eq("eclipse"),
            eq("https://raw.githubusercontent.com/eclipse/che/master/.factory.dockerfile"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue();

    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "git");
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

    FactoryDto computedFactory = newDto(FactoryDto.class).withV("4.0");
    when(urlFactoryBuilder.createFactory(anyString())).thenReturn(computedFactory);

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following codenvy json file
    verify(urlFactoryBuilder).createFactory(jsonFileLocationArgumentCaptor.capture());
    assertEquals(
        jsonFileLocationArgumentCaptor.getValue(),
        "https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder)
        .buildWorkspaceConfig(
            eq("che"),
            eq("eclipse"),
            eq("https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.dockerfile"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue();
    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "git");
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

    FactoryDto computedFactory = newDto(FactoryDto.class).withV("4.0");
    when(urlFactoryBuilder.createFactory(anyString())).thenReturn(computedFactory);

    githubFactoryParametersResolver.createFactory(singletonMap(URL_PARAMETER_NAME, githubUrl));

    // check we called the builder with the following codenvy json file
    verify(urlFactoryBuilder).createFactory(jsonFileLocationArgumentCaptor.capture());
    assertEquals(
        jsonFileLocationArgumentCaptor.getValue(),
        "https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.json");

    // check we provide dockerfile and correct env
    verify(urlFactoryBuilder)
        .buildWorkspaceConfig(
            eq("che"),
            eq("eclipse"),
            eq("https://raw.githubusercontent.com/eclipse/che/4.2.x/.factory.dockerfile"));

    // check project config built
    verify(projectConfigDtoMerger)
        .merge(any(FactoryDto.class), projectConfigDtoArgumentCaptor.capture());

    ProjectConfigDto projectConfigDto = projectConfigDtoArgumentCaptor.getValue();
    SourceStorageDto sourceStorageDto = projectConfigDto.getSource();
    assertNotNull(sourceStorageDto);
    assertEquals(sourceStorageDto.getType(), "git");
    assertEquals(sourceStorageDto.getLocation(), githubCloneUrl);
    Map<String, String> sourceParameters = sourceStorageDto.getParameters();
    assertEquals(sourceParameters.size(), 2);
    assertEquals(sourceParameters.get("branch"), githubBranch);
    assertEquals(sourceParameters.get("keepDir"), githubKeepdir);
  }
}
