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
package org.eclipse.che.plugin.maven.server.projecttype;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Vitalii Parfonov */
@Listeners(value = {MockitoTestNGListener.class})
public class MavenValueProviderTest {

  String pomContent =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
          + "    <parent>\n"
          + "        <artifactId>che-plugin-parent</artifactId>\n"
          + "        <groupId>org.eclipse.che.plugin</groupId>\n"
          + "        <version>5.0.0-SNAPSHOT</version>\n"
          + "    </parent>"
          + "    <modelVersion>4.0.0</modelVersion>\n"
          + "    <groupId>my_group</groupId>\n"
          + "    <artifactId>my_artifact</artifactId>\n"
          + "    <version>1.0-SNAPSHOT</version>\n"
          + "    <packaging>jar</packaging>\n"
          + "    <build>\n"
          + "        <sourceDirectory>src</sourceDirectory>"
          + "        <testSourceDirectory>test</testSourceDirectory>"
          + "     </build>\n"
          + "</project>";

  @Mock private MavenProjectManager mavenProjectManager;
  @Mock private FolderEntry folderEntry;
  @Mock private MavenProject mavenProject;
  @Mock private MavenKey mavenKey;
  @Mock private MavenKey parentKey;

  private MavenValueProvider mavenValueProvider;

  @BeforeMethod
  public void setUp() {
    when(folderEntry.getPath()).thenReturn(Path.of(""));
    when(mavenProject.getMavenKey()).thenReturn(mavenKey);
    when(mavenProject.getParentKey()).thenReturn(parentKey);
    mavenValueProvider = new MavenValueProvider(mavenProjectManager, folderEntry);
  }

  @Test
  public void getArtifactIdFromMavenProject() throws Exception {
    String artifactId = NameGenerator.generate("artifactId-", 6);
    when(mavenKey.getArtifactId()).thenReturn(artifactId);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> artifactIds = mavenValueProvider.getValues(MavenAttributes.ARTIFACT_ID);
    Assert.assertNotNull(artifactIds);
    Assert.assertFalse(artifactIds.isEmpty());
    Assert.assertEquals(artifactIds.size(), 1);
    Assert.assertNotNull(artifactIds.get(0));
    Assert.assertEquals(artifactIds.get(0), artifactId);
  }

  @Test
  public void getGroupIdFromMavenProject() throws Exception {
    String groupId = NameGenerator.generate("groupId-", 6);
    when(mavenKey.getGroupId()).thenReturn(groupId);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> groupIds = mavenValueProvider.getValues(MavenAttributes.GROUP_ID);
    Assert.assertNotNull(groupIds);
    Assert.assertFalse(groupIds.isEmpty());
    Assert.assertEquals(groupIds.size(), 1);
    Assert.assertNotNull(groupIds.get(0));
    Assert.assertEquals(groupIds.get(0), groupId);
  }

  @Test
  public void getVersionFromMavenProject() throws Exception {
    String versionId = NameGenerator.generate("version-", 6);
    when(mavenKey.getVersion()).thenReturn(versionId);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> versions = mavenValueProvider.getValues(MavenAttributes.VERSION);
    Assert.assertNotNull(versions);
    Assert.assertFalse(versions.isEmpty());
    Assert.assertEquals(versions.size(), 1);
    Assert.assertNotNull(versions.get(0));
    Assert.assertEquals(versions.get(0), versionId);
  }

  @Test
  public void getPackagingFromMavenProject() throws Exception {
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    when(mavenProject.getPackaging()).thenReturn("war");
    List<String> pkgs = mavenValueProvider.getValues(MavenAttributes.PACKAGING);
    Assert.assertNotNull(pkgs);
    Assert.assertFalse(pkgs.isEmpty());
    Assert.assertEquals(pkgs.size(), 1);
    Assert.assertNotNull(pkgs.get(0));
    Assert.assertEquals(pkgs.get(0), "war");
  }

  @Test
  public void getPackagingFromMavenProjectIfNotSet() throws Exception {
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> pkgs = mavenValueProvider.getValues(MavenAttributes.PACKAGING);
    Assert.assertNotNull(pkgs);
    Assert.assertFalse(pkgs.isEmpty());
    Assert.assertEquals(pkgs.size(), 1);
    Assert.assertNotNull(pkgs.get(0));
    Assert.assertEquals(pkgs.get(0), "jar");
  }

  @Test
  public void getParentArtifactFromMavenProject() throws Exception {
    String parentArtifact = NameGenerator.generate("parentArtifact", 6);
    when(parentKey.getArtifactId()).thenReturn(parentArtifact);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> values = mavenValueProvider.getValues(MavenAttributes.PARENT_ARTIFACT_ID);
    Assert.assertNotNull(values);
    Assert.assertFalse(values.isEmpty());
    Assert.assertEquals(values.size(), 1);
    Assert.assertNotNull(values.get(0));
    Assert.assertEquals(values.get(0), parentArtifact);
  }

  @Test
  public void getParentVersionFromMavenProject() throws Exception {
    String parentVersionId = NameGenerator.generate("parent-version-", 6);
    when(parentKey.getVersion()).thenReturn(parentVersionId);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> versions = mavenValueProvider.getValues(MavenAttributes.PARENT_VERSION);
    Assert.assertNotNull(versions);
    Assert.assertFalse(versions.isEmpty());
    Assert.assertEquals(versions.size(), 1);
    Assert.assertNotNull(versions.get(0));
    Assert.assertEquals(versions.get(0), parentVersionId);
  }

  @Test
  public void getParentGroupFromMavenProject() throws Exception {
    String groupId = NameGenerator.generate("parent-group-", 6);
    when(parentKey.getGroupId()).thenReturn(groupId);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> values = mavenValueProvider.getValues(MavenAttributes.PARENT_GROUP_ID);
    Assert.assertNotNull(values);
    Assert.assertFalse(values.isEmpty());
    Assert.assertEquals(values.size(), 1);
    Assert.assertNotNull(values.get(0));
    Assert.assertEquals(values.get(0), groupId);
  }

  @Test
  public void getSourceFromMavenProject() throws Exception {
    final List<String> strings = singletonList("src");
    when(mavenProject.getSources()).thenReturn(strings);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> sources = mavenValueProvider.getValues(Constants.SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, strings);
  }

  @Test
  public void getSourceFromMavenProjectIfNotSet() throws Exception {
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> sources = mavenValueProvider.getValues(Constants.SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList(MavenAttributes.DEFAULT_SOURCE_FOLDER));
  }

  @Test
  public void getTestSourceFromMavenProject() throws Exception {
    List<String> strings = singletonList("src/test");
    when(mavenProject.getTestSources()).thenReturn(strings);
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> sources = mavenValueProvider.getValues(MavenAttributes.TEST_SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, strings);
  }

  @Test
  public void getTestSourceFromMavenProjectIfNotSet() throws Exception {
    when(mavenProjectManager.getMavenProject(anyString())).thenReturn(mavenProject);
    List<String> sources = mavenValueProvider.getValues(MavenAttributes.TEST_SOURCE_FOLDER);
    verify(mavenProjectManager).getMavenProject(anyString());
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList(MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER));
  }

  @Test
  public void getArtifactIdFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> artifactIds = mavenValueProvider.getValues(MavenAttributes.ARTIFACT_ID);
    Assert.assertNotNull(artifactIds);
    Assert.assertFalse(artifactIds.isEmpty());
    Assert.assertEquals(artifactIds.size(), 1);
    Assert.assertNotNull(artifactIds.get(0));
    Assert.assertEquals(artifactIds.get(0), "my_artifact");
  }

  @Test
  public void getGroupIdFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> groupIds = mavenValueProvider.getValues(MavenAttributes.GROUP_ID);
    Assert.assertNotNull(groupIds);
    Assert.assertFalse(groupIds.isEmpty());
    Assert.assertEquals(groupIds.size(), 1);
    Assert.assertNotNull(groupIds.get(0));
    Assert.assertEquals(groupIds.get(0), "my_group");
  }

  @Test
  public void getVersionFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> versions = mavenValueProvider.getValues(MavenAttributes.VERSION);
    Assert.assertNotNull(versions);
    Assert.assertFalse(versions.isEmpty());
    Assert.assertEquals(versions.size(), 1);
    Assert.assertNotNull(versions.get(0));
    Assert.assertEquals(versions.get(0), "1.0-SNAPSHOT");
  }

  @Test
  public void getPackagingFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> pkgs = mavenValueProvider.getValues(MavenAttributes.PACKAGING);
    Assert.assertNotNull(pkgs);
    Assert.assertFalse(pkgs.isEmpty());
    Assert.assertEquals(pkgs.size(), 1);
    Assert.assertNotNull(pkgs.get(0));
    Assert.assertEquals(pkgs.get(0), "jar");
  }

  @Test
  public void getPackagingFromPomIfNotSet() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>";
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> pkgs = mavenValueProvider.getValues(MavenAttributes.PACKAGING);
    Assert.assertNotNull(pkgs);
    Assert.assertFalse(pkgs.isEmpty());
    Assert.assertEquals(pkgs.size(), 1);
    Assert.assertNotNull(pkgs.get(0));
    Assert.assertEquals(pkgs.get(0), "jar");
  }

  @Test
  public void getParentArtifactFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> values = mavenValueProvider.getValues(MavenAttributes.PARENT_ARTIFACT_ID);
    Assert.assertNotNull(values);
    Assert.assertFalse(values.isEmpty());
    Assert.assertEquals(values.size(), 1);
    Assert.assertNotNull(values.get(0));
    Assert.assertEquals(values.get(0), "che-plugin-parent");
  }

  @Test
  public void getParentVersionFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> versions = mavenValueProvider.getValues(MavenAttributes.PARENT_VERSION);
    Assert.assertNotNull(versions);
    Assert.assertFalse(versions.isEmpty());
    Assert.assertEquals(versions.size(), 1);
    Assert.assertNotNull(versions.get(0));
    Assert.assertEquals(versions.get(0), "5.0.0-SNAPSHOT");
  }

  @Test
  public void getParentGroupFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> values = mavenValueProvider.getValues(MavenAttributes.PARENT_GROUP_ID);
    Assert.assertNotNull(values);
    Assert.assertFalse(values.isEmpty());
    Assert.assertEquals(values.size(), 1);
    Assert.assertNotNull(values.get(0));
    Assert.assertEquals(values.get(0), "org.eclipse.che.plugin");
  }

  @Test
  public void getSourceFromPom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> sources = mavenValueProvider.getValues(Constants.SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList("src"));
  }

  @Test
  public void getSourceFromPomIfNotSet() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>";
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> sources = mavenValueProvider.getValues(Constants.SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList(MavenAttributes.DEFAULT_SOURCE_FOLDER));
  }

  @Test
  public void getTestSourcePom() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> sources = mavenValueProvider.getValues(MavenAttributes.TEST_SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList("test"));
  }

  @Test
  public void getTestSourceFromPomIfNotSet() throws Exception {
    FileEntry fileEntry = mock(FileEntry.class);
    String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>";
    when(fileEntry.getInputStream())
        .thenReturn(new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8)));
    when(folderEntry.getChild(anyString())).thenReturn(fileEntry);
    List<String> sources = mavenValueProvider.getValues(MavenAttributes.TEST_SOURCE_FOLDER);
    Assert.assertNotNull(sources);
    Assert.assertFalse(sources.isEmpty());
    Assert.assertEquals(sources.size(), 1);
    Assert.assertEquals(sources, singletonList(MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER));
  }
}
