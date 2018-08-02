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
package org.eclipse.che.commons.lang;

import static org.testng.Assert.*;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

public class UrlUtilsTest {
  @Test
  public void shouldExtractParametersWithoutValue() throws Exception {
    Map<String, List<String>> params =
        UrlUtils.getQueryParameters(new URL("http://codenvy.com/factory?v"));
    assertTrue(params.containsKey("v"));
    assertNull(params.get("v").iterator().next());
  }

  @Test
  public void shouldExtractParametersWithMultipleValues() throws Exception {
    Map<String, List<String>> expectedParams = new HashMap<>();
    List<String> v = new LinkedList<>();
    v.add("123");
    v.add("qwe");
    v.add("www");
    expectedParams.put("v", v);

    Map<String, List<String>> params =
        UrlUtils.getQueryParameters(new URL("http://codenvy.com/factory?v=123&v=qwe&v=www"));

    assertEquals(params, expectedParams);
  }

  @Test
  public void shouldExtractParametersWithMultipleValuesDividedAnotherParameters() throws Exception {
    Map<String, List<String>> expectedParams = new HashMap<>();
    List<String> v = new LinkedList<>();
    v.add("123");
    v.add("qwe");
    v.add("www");
    List<String> par = new LinkedList<>();
    par.add("test");
    expectedParams.put("v", v);
    expectedParams.put("par", par);

    Map<String, List<String>> params =
        UrlUtils.getQueryParameters(
            new URL("http://codenvy.com/factory?v=123&par=test&v=qwe&v=www"));

    assertEquals(params, expectedParams);
  }

  @Test
  public void shouldIgnoreSlashAtTheEndOfPath() throws Exception {
    Map<String, List<String>> expectedParams = new HashMap<>();
    List<String> v = new LinkedList<>();
    v.add("123");
    v.add("qwe");
    v.add("www");
    List<String> par = new LinkedList<>();
    par.add("test");
    expectedParams.put("v", v);
    expectedParams.put("par", par);

    Map<String, List<String>> params =
        UrlUtils.getQueryParameters(
            new URL("http://codenvy.com/factory/?v=123&par=test&v=qwe&v=www"));

    assertEquals(params, expectedParams);
  }

  @Test
  public void shouldExtractEncodedParameters() throws Exception {
    Map<String, List<String>> expectedParams = new HashMap<>();
    List<String> vcsurl = new LinkedList<>();
    vcsurl.add("http://github/some/path?somequery=qwe&somequery=sss&somequery=rty");
    expectedParams.put("vcsurl", vcsurl);

    Map<String, List<String>> params =
        UrlUtils.getQueryParameters(
            new URL(
                "http://codenvy.com/factory?vcsurl="
                    + URLEncoder.encode(
                        "http://github/some/path?somequery=qwe&somequery=sss&somequery=rty",
                        "UTF-8")));

    assertEquals(params, expectedParams);
  }
}
