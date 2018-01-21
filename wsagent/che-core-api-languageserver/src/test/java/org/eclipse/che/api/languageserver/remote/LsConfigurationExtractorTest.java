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
package org.eclipse.che.api.languageserver.remote;

import static org.testng.Assert.*;

import com.google.gson.JsonParser;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link LsConfigurationExtractor} */
@Listeners(MockitoTestNGListener.class)
public class LsConfigurationExtractorTest {

  private LsConfigurationExtractor lsConfigurationExtractor;

  @BeforeMethod
  public void setUp() throws Exception {
    lsConfigurationExtractor = new LsConfigurationExtractor(new JsonParser());
  }

  @Test
  public void shouldExtractId() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config", "{\"id\":\"testId\" ,\"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(languageServerDescription.getId(), "testId");
  }

  @Test
  public void shouldExtractNullWhenIdIsNotMentioned() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap("config", "{\"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertNull(languageServerDescription.getId());
  }

  @Test
  public void shouldExtractLanguageIds() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap("config", "{\"languageIds\": [\"languageId\"] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(
        languageServerDescription.getLanguageIds(), Collections.singletonList("languageId"));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionWhenExtractEmptyLanguageIdsForEmptyArray() throws Exception {
    Map<String, String> attributes = Collections.singletonMap("config", "{\"languageIds\": [] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getLanguageIds().isEmpty());
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void shouldThrowExceptionWhenExtractEmptyLanguageIdsWhenLanguageIdsAreNotMentioned()
      throws Exception {
    Map<String, String> attributes = Collections.singletonMap("config", "{ }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getLanguageIds().isEmpty());
  }

  @Test
  public void shouldExtractFileWatchPatterns() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config",
            "{\"fileWatchPatterns\": [\"fileWatchPattern\"], \"languageIds\": [\"languageId\"] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(
        languageServerDescription.getFileWatchPatterns(),
        Collections.singletonList("fileWatchPattern"));
  }

  @Test
  public void shouldExtractEmptyFileWatchPatternsForEmptyArray() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config", "{\"fileWatchPatterns\": [] , \"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getFileWatchPatterns().isEmpty());
  }

  @Test
  public void shouldExtractEmptyFileWatchPatternsWhenFileWatchPatternsAreNotMentioned()
      throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap("config", "{\"languageIds\": [\"languageId\"] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getFileWatchPatterns().isEmpty());
  }

  @Test
  public void shouldExtractDocumentFilter() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config", "{\"documentFilters\": [{}],\"languageIds\": [\"languageId\"] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(languageServerDescription.getDocumentFilters().size(), 1);
  }

  @Test
  public void shouldExtractEmptyDocumentFilterForEmptyArray() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config", "{\"documentFilters\": [] ,\"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getDocumentFilters().isEmpty());
  }

  @Test
  public void shouldExtractDocumentFilterWhenDocumentFilterAreNotMentioned() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap("config", "{ \"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertTrue(languageServerDescription.getDocumentFilters().isEmpty());
  }

  @Test
  public void shouldExtractDocumentFilterLanguageId() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config",
            "{\"documentFilters\": [{\"languageId\":\"testId\"}] ,\"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(languageServerDescription.getDocumentFilters().size(), 1);
    assertEquals(languageServerDescription.getDocumentFilters().get(0).getLanguageId(), "testId");
  }

  @Test
  public void shouldExtractDocumentFilterScheme() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config",
            "{\"documentFilters\": [{\"scheme\":\"testScheme\"}],\"languageIds\": [\"languageId\"] }");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(languageServerDescription.getDocumentFilters().size(), 1);
    assertEquals(languageServerDescription.getDocumentFilters().get(0).getScheme(), "testScheme");
  }

  @Test
  public void shouldExtractDocumentFilterPathRegex() throws Exception {
    Map<String, String> attributes =
        Collections.singletonMap(
            "config",
            "{\"documentFilters\": [{\"pathRegex\":\"testPathRegex\"}] ,\"languageIds\": [\"languageId\"]}");

    LanguageServerDescription languageServerDescription =
        lsConfigurationExtractor.extract(attributes);

    assertEquals(languageServerDescription.getDocumentFilters().size(), 1);
    assertEquals(
        languageServerDescription.getDocumentFilters().get(0).getPathRegex(), "testPathRegex");
  }
}
