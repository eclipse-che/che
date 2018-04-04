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
package org.eclipse.che.commons.lang;

import static org.testng.Assert.*;

import java.net.URI;
import java.util.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestURLEncodedUtils {

  static final int SWISS_GERMAN_HELLO[] = {
    0x47, 0x72, 0xFC, 0x65, 0x7A, 0x69, 0x5F, 0x7A, 0xE4, 0x6D, 0xE4
  };
  static final int RUSSIAN_HELLO[] = {
    0x412, 0x441, 0x435, 0x43C, 0x5F, 0x43F, 0x440, 0x438, 0x432, 0x435, 0x442
  };

  private static String constructString(int[] unicodeChars) {
    StringBuffer buffer = new StringBuffer();
    if (unicodeChars != null) {
      for (int i = 0; i < unicodeChars.length; i++) {
        buffer.append((char) unicodeChars[i]);
      }
    }
    return buffer.toString();
  }

  private static void assertNameValuePair(
      final Map<String, Set<String>> parameter, final String expectedName, final String... values) {
    Set<String> actualValues = parameter.get(expectedName);
    assertNotNull(actualValues);
    assertEquals(actualValues.size(), values.length);
    for (String value : values) {
      assertTrue(actualValues.contains(value));
    }
  }

  @Test
  public void shouldBeAbleToParseEmptyURI() throws Exception {
    // given
    // when

    Map<String, Set<String>> parameters = parse("", null);
    // then
    assertTrue(parameters.isEmpty());
  }

  @Test(dataProvider = "uris")
  public void shouldParseURI(String url, String expectedName, String[] expectedValues)
      throws Exception {

    assertNameValuePair(parse(url, "UTF-8"), expectedName, expectedValues);
  }

  @Test(dataProvider = "uris")
  public void shouldFormatURI(String url, String expectedName, String[] expectedValues)
      throws Exception {
    // given
    Map<String, Set<String>> parameters = new HashMap<>();
    parameters.put(expectedName, new LinkedHashSet<>(Arrays.asList(expectedValues)));
    // when
    String actual = URLEncodedUtils.format(parameters, "UTF-8");
    // then
    if ("Name3".equals(url)) {
      assertEquals(actual, url + "=");
    } else {
      assertEquals(actual, url);
    }
  }

  @Test
  public void shouldFormatURIWithMultipleParams() throws Exception {

    // given
    Map<String, Set<String>> parameters = new LinkedHashMap<>();
    parameters.put("Name5", new LinkedHashSet<>(Arrays.asList(new String[] {"aaa"})));
    parameters.put("Name6", new LinkedHashSet<>(Arrays.asList(new String[] {"bbb"})));
    // when
    String actual = URLEncodedUtils.format(parameters, "UTF-8");
    // then
    assertEquals(actual, "Name5=aaa&Name6=bbb");
  }

  @Test
  public void shouldParseURIWithMultipleParams() throws Exception {

    Map<String, Set<String>> parameters = parse("Name5=aaa&Name6=bbb", null);
    assertNameValuePair(parameters, "Name5", "aaa");
    assertNameValuePair(parameters, "Name6", "bbb");
  }

  @DataProvider(name = "uris")
  public Object[][] uris() {
    return new Object[][] {
      {"Name1=Value1", "Name1", new String[] {"Value1"}},
      {"Name2=", "Name2", new String[] {}},
      {"Name3", "Name3", new String[] {}},
      {"Name4=Value+4%21", "Name4", new String[] {"Value 4!"}},
      {"Name4=Value%2B4%21", "Name4", new String[] {"Value+4!"}},
      {"Name4=Value+4%21+%214", "Name4", new String[] {"Value 4! !4"}},
      {"Name7=aaa&Name7=b%2Cb&Name7=ccc", "Name7", new String[] {"aaa", "b,b", "ccc"}},
      {"Name8=xx%2C++yy++%2Czz", "Name8", new String[] {"xx,  yy  ,zz"}},
    };
  }

  @Test
  public void shouldFormatUTF8() {
    // given
    String ru_hello = constructString(RUSSIAN_HELLO);
    String ch_hello = constructString(SWISS_GERMAN_HELLO);

    Map<String, Set<String>> parameters = new LinkedHashMap<>();
    parameters.put("russian", new LinkedHashSet<>(Arrays.asList(new String[] {ru_hello})));
    parameters.put("swiss", new LinkedHashSet<>(Arrays.asList(new String[] {ch_hello})));
    // when
    String actual = URLEncodedUtils.format(parameters, "UTF-8");
    // then
    assertEquals(
        actual,
        "russian=%D0%92%D1%81%D0%B5%D0%BC_%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82"
            + "&swiss=Gr%C3%BCezi_z%C3%A4m%C3%A4");
  }

  @Test
  public void shouldParseUTF8() {
    // given
    String ru_hello = constructString(RUSSIAN_HELLO);
    String ch_hello = constructString(SWISS_GERMAN_HELLO);

    // when
    Map<String, Set<String>> parameters =
        parse(
            "russian=%D0%92%D1%81%D0%B5%D0%BC_%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82"
                + "&swiss=Gr%C3%BCezi_z%C3%A4m%C3%A4",
            "UTF-8");
    // then
    assertNameValuePair(parameters, "russian", ru_hello);
    assertNameValuePair(parameters, "swiss", ch_hello);
  }

  @Test
  public void shouldParseWithoutDecoding() {
    // given
    String ru_hello = "%D0%92%D1%81%D0%B5%D0%BC_%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82";
    String ch_hello = "Gr%C3%BCezi_z%C3%A4m%C3%A4";

    // when
    Map<String, Set<String>> parameters =
        URLEncodedUtils.parse(
            URI.create("http://hc.apache.org/params?russian=" + ru_hello + "&swiss=" + ch_hello),
            false);
    // then
    assertNameValuePair(parameters, "russian", ru_hello);
    assertNameValuePair(parameters, "swiss", ch_hello);
  }

  private Map<String, Set<String>> parse(final String params, final String encoding) {
    return URLEncodedUtils.parse(URI.create("http://hc.apache.org/params?" + params), encoding);
  }
}
