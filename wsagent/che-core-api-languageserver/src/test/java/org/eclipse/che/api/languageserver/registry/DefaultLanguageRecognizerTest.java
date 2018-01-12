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
package org.eclipse.che.api.languageserver.registry;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.registry.LanguageRecognizer.UNIDENTIFIED;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link DefaultLanguageRecognizer} */
@Listeners(MockitoTestNGListener.class)
public class DefaultLanguageRecognizerTest {

  @Spy private Set<LanguageDescription> languages = new HashSet<>();
  @Mock private DefaultLanguages defaultLanguages;

  @InjectMocks private DefaultLanguageRecognizer recognizer;

  @BeforeMethod
  public void setUp() {
    languages.clear();
  }

  @Test
  public void shouldRecognizeDefaultByName() throws Exception {
    LanguageDescription expected = new LanguageDescription();
    expected.setFileNames(singletonList("Dockerfile"));
    when(defaultLanguages.getAll()).thenReturn(Collections.singleton(expected));

    LanguageDescription language = recognizer.recognizeByPath("/project/Dockerfile");

    assertEquals(language, expected);
  }

  @Test
  public void shouldRecognizeDefaultByExtension() throws Exception {
    LanguageDescription expected = new LanguageDescription();
    expected.setFileExtensions(singletonList("java"));
    when(defaultLanguages.getAll()).thenReturn(Collections.singleton(expected));

    LanguageDescription language = recognizer.recognizeByPath("/project/Main.java");

    assertEquals(language, expected);
  }

  @Test
  public void shouldRecognizeOverriddenByName() throws Exception {
    LanguageDescription expected = new LanguageDescription();
    expected.setFileNames(singletonList("Dockerfile"));
    languages.add(expected);

    LanguageDescription language = recognizer.recognizeByPath("/project/Dockerfile");

    assertEquals(language, expected);
  }

  @Test
  public void shouldRecognizeOverriddenByExtension() throws Exception {
    LanguageDescription expected = new LanguageDescription();
    expected.setFileExtensions(singletonList("java"));
    languages.add(expected);

    LanguageDescription language = recognizer.recognizeByPath("/project/Main.java");

    assertEquals(language, expected);
  }

  @Test
  public void shouldNotRecognizeByNameWhenNoLanguageSet() throws Exception {
    LanguageDescription language = recognizer.recognizeByPath("/project/Dockerfile");

    assertEquals(language, UNIDENTIFIED);
  }

  @Test
  public void shouldNotRecognizeByExtensionWhenNoLanguageSet() throws Exception {
    LanguageDescription language = recognizer.recognizeByPath("/project/Main.java");

    assertEquals(language, UNIDENTIFIED);
  }

  @Test
  public void shouldRecognizeOverriddenPriorToDefaultByName() throws Exception {
    LanguageDescription notExpected = new LanguageDescription();
    notExpected.setFileNames(singletonList("Dockerfile"));
    notExpected.setLanguageId("expectedId");
    when(defaultLanguages.getAll()).thenReturn(Collections.singleton(notExpected));

    LanguageDescription expected = new LanguageDescription();
    expected.setFileNames(singletonList("Dockerfile"));
    expected.setLanguageId("notExpectedId");
    languages.add(expected);

    LanguageDescription language = recognizer.recognizeByPath("/project/Dockerfile");

    assertEquals(language, expected);
    assertNotEquals(language, notExpected);
  }

  @Test
  public void shouldRecognizeOverriddenPriorToDefaultByExtension() throws Exception {
    LanguageDescription notExpected = new LanguageDescription();
    notExpected.setFileExtensions(singletonList("java"));
    notExpected.setLanguageId("expectedId");
    when(defaultLanguages.getAll()).thenReturn(Collections.singleton(notExpected));

    LanguageDescription expected = new LanguageDescription();
    expected.setFileExtensions(singletonList("java"));
    expected.setLanguageId("notExpectedId");
    languages.add(expected);

    LanguageDescription language = recognizer.recognizeByPath("/project/Main.java");

    assertEquals(language, expected);
    assertNotEquals(language, notExpected);
  }
}
