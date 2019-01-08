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
package org.eclipse.che.core.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ApiResponseCounterTest {
  private ApiResponseCounter apiResponseCounter;
  private MeterRegistry registry;

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry();

    apiResponseCounter = new ApiResponseCounter();
    apiResponseCounter.bindTo(registry);
  }

  @Test
  public void test1xxResponses() {
    apiResponseCounter.handleStatus(500);

    RequiredSearch meter = registry.get("che.server.api.response");
    registry.getMeters();
  }

  @Test
  public void test2xxResponses() {
    apiResponseCounter.handleStatus(500);

    RequiredSearch meter = registry.get("che.server.api.response");
    registry.getMeters();
  }

  @Test
  public void test3xxResponses() {
    apiResponseCounter.handleStatus(500);

    RequiredSearch meter = registry.get("che.server.api.response");
    registry.getMeters();
  }

  @Test
  public void test4xxResponses() {
    apiResponseCounter.handleStatus(500);

    RequiredSearch meter = registry.get("che.server.api.response");
    registry.getMeters();
  }

  @Test
  public void testServerError() {
    apiResponseCounter.handleStatus(500);

    RequiredSearch meter = registry.get("che.server.api.response");
    registry.getMeters();
  }

  @DataProvider(name = "information")
  public Object[][] information() {
    return new Object[][] {{100}, {101}};
  }

  @DataProvider(name = "successes")
  public Object[][] success() {
    return new Object[][] {{200}, {201}, {202}, {203}, {204}};
  }

  @DataProvider(name = "redirects")
  public Object[][] redirects() {
    return new Object[][] {{300}, {301}, {302}, {303}, {304}};
  }

  @DataProvider(name = "clientErrors")
  public Object[][] clientErrors() {
    return new Object[][] {{400}, {401}, {402}, {403}, {404}, {405}};
  }

  @DataProvider(name = "serverErrors")
  public Object[][] serverErrors() {
    return new Object[][] {{500}, {501}, {502}, {503}, {504}};
  }
}
