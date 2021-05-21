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
package org.eclipse.che.api.factory.server.gitlab;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.model.factory.ScmInfo;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.urlfactory.DevfileFilenamesProvider;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.shared.dto.FactoryDevfileV2Dto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.MetadataDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.SourceDto;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Validate operations performed by the Github Factory service
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GitlabFactoryParametersResolverTest {

  @Mock private URLFactoryBuilder urlFactoryBuilder;

  @Mock private URLFetcher urlFetcher;

  @Mock private DevfileFilenamesProvider devfileFilenamesProvider;

  GitlabUrlParser gitlabUrlParser;

  @Mock private GitCredentialManager gitCredentialManager;
  @Mock private PersonalAccessTokenManager personalAccessTokenManager;

  private GitlabFactoryParametersResolver gitlabFactoryParametersResolver;

  @BeforeMethod
  protected void init() {
    gitlabUrlParser = new GitlabUrlParser("http://gitlab.2mcl.com", devfileFilenamesProvider);
    assertNotNull(this.gitlabUrlParser);
    gitlabFactoryParametersResolver =
        new GitlabFactoryParametersResolver(
            urlFactoryBuilder,
            urlFetcher,
            gitlabUrlParser,
            gitCredentialManager,
            personalAccessTokenManager);
    assertNotNull(this.gitlabFactoryParametersResolver);
  }

  /** Check url which is not a bitbucket url can't be accepted by this resolver */
  @Test
  public void checkInvalidAcceptUrl() {
    Map<String, String> parameters = singletonMap(URL_PARAMETER_NAME, "http://github.com");
    // shouldn't be accepted
    assertFalse(gitlabFactoryParametersResolver.accept(parameters));
  }

  /** Check bitbucket url will be be accepted by this resolver */
  @Test
  public void checkValidAcceptUrl() {
    Map<String, String> parameters =
        singletonMap(URL_PARAMETER_NAME, "http://gitlab.2mcl.com/test/proj/repo.git");
    // should be accepted
    assertTrue(gitlabFactoryParametersResolver.accept(parameters));
  }

  @Test
  public void shouldGenerateDevfileForFactoryWithNoDevfileOrJson() throws Exception {

    String gitlabUrl = "http://gitlab.2mcl.com/test/proj/repo.git";

    FactoryDto computedFactory = generateDevfileFactory();

    when(urlFactoryBuilder.buildDefaultDevfile(any())).thenReturn(computedFactory.getDevfile());

    when(urlFactoryBuilder.createFactoryFromDevfile(any(RemoteFactoryUrl.class), any(), anyMap()))
        .thenReturn(Optional.empty());
    Map<String, String> params = ImmutableMap.of(URL_PARAMETER_NAME, gitlabUrl);
    // when
    FactoryDto factory = (FactoryDto) gitlabFactoryParametersResolver.createFactory(params);
    // then
    verify(urlFactoryBuilder).buildDefaultDevfile(eq("proj"));
    assertEquals(factory, computedFactory);
    SourceDto source = factory.getDevfile().getProjects().get(0).getSource();
    assertEquals(source.getLocation(), gitlabUrl);
    assertNull(source.getBranch());
  }

  @Test
  public void shouldSetDefaultProjectIntoDevfileIfNotSpecified() throws Exception {

    String gitlabUrl = "http://gitlab.2mcl.com/test/proj/repo/-/tree/foobar";

    FactoryDto computedFactory = generateDevfileFactory();

    when(urlFactoryBuilder.createFactoryFromDevfile(any(RemoteFactoryUrl.class), any(), anyMap()))
        .thenReturn(Optional.of(computedFactory));

    Map<String, String> params = ImmutableMap.of(URL_PARAMETER_NAME, gitlabUrl);
    // when
    FactoryDto factory = (FactoryDto) gitlabFactoryParametersResolver.createFactory(params);
    // then
    assertNotNull(factory.getDevfile());
    SourceDto source = factory.getDevfile().getProjects().get(0).getSource();
    assertEquals(source.getLocation(), "http://gitlab.2mcl.com/test/proj/repo.git");
    assertEquals(source.getBranch(), "foobar");
  }

  @Test
  public void shouldSetScmInfoIntoDevfileV2() throws Exception {

    String gitlabUrl = "http://gitlab.2mcl.com/eclipse/che/-/tree/foobar";

    FactoryDevfileV2Dto computedFactory = generateDevfileV2Factory();

    when(urlFactoryBuilder.createFactoryFromDevfile(any(RemoteFactoryUrl.class), any(), anyMap()))
        .thenReturn(Optional.of(computedFactory));

    Map<String, String> params = ImmutableMap.of(URL_PARAMETER_NAME, gitlabUrl);
    // when
    FactoryDevfileV2Dto factory =
        (FactoryDevfileV2Dto) gitlabFactoryParametersResolver.createFactory(params);
    // then
    ScmInfo scmInfo = factory.getScmInfo();
    assertNotNull(scmInfo);
    assertEquals(scmInfo.getScmProviderName(), "gitlab");
    assertEquals(scmInfo.getRepositoryUrl(), "http://gitlab.2mcl.com/eclipse/che.git");
    assertEquals(scmInfo.getBranch(), "foobar");
  }

  private FactoryDto generateDevfileFactory() {
    return newDto(FactoryDto.class)
        .withV(CURRENT_VERSION)
        .withSource("repo")
        .withDevfile(
            newDto(DevfileDto.class)
                .withApiVersion(CURRENT_API_VERSION)
                .withMetadata(newDto(MetadataDto.class).withName("che")));
  }

  private FactoryDevfileV2Dto generateDevfileV2Factory() {
    return newDto(FactoryDevfileV2Dto.class)
        .withV(CURRENT_VERSION)
        .withSource("repo")
        .withDevfile(Map.of("schemaVersion", "2.0.0"));
  }
}
