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
package org.eclipse.che.core.metrics;

import static org.testng.Assert.assertEquals;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test for {@link ApiResponseCounter} functionality
 *
 * @author Mykhailo Kuznietsov
 */
public class ApiResponseCounterTest {
  private ApiResponseCounter apiResponseCounter;
  private MeterRegistry registry;

  @BeforeMethod
  public void setup() {
    registry = new SimpleMeterRegistry();

    apiResponseCounter = new ApiResponseCounter();
    apiResponseCounter.bindTo(registry);
  }

  @Test(dataProvider = "information")
  public void shouldCount1xxResponses(int status) {
    apiResponseCounter.handleStatus(status);

    assertEquals(apiResponseCounter.informationalResponseCounter.count(), 1.0);
    assertEquals(apiResponseCounter.successResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.redirectResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.clientErrorResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.serverErrorResponseCounter.count(), 0.0);
  }

  @Test(dataProvider = "success")
  public void shouldCount2xxResponses(int status) {
    apiResponseCounter.handleStatus(status);

    assertEquals(apiResponseCounter.informationalResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.successResponseCounter.count(), 1.0);
    assertEquals(apiResponseCounter.redirectResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.clientErrorResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.serverErrorResponseCounter.count(), 0.0);
  }

  @Test(dataProvider = "redirect")
  public void shouldCount3xxResponses(int status) {
    apiResponseCounter.handleStatus(status);

    assertEquals(apiResponseCounter.informationalResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.successResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.redirectResponseCounter.count(), 1.0);
    assertEquals(apiResponseCounter.clientErrorResponseCounter.count(), .0);
    assertEquals(apiResponseCounter.serverErrorResponseCounter.count(), 0.0);
  }

  @Test(dataProvider = "clientError")
  public void shouldCount4xxResponses(int status) {
    apiResponseCounter.handleStatus(status);

    assertEquals(apiResponseCounter.informationalResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.successResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.redirectResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.clientErrorResponseCounter.count(), 1.0);
    assertEquals(apiResponseCounter.serverErrorResponseCounter.count(), 0.0);
  }

  @Test(dataProvider = "serverError")
  public void shouldCount5xxResponses(int status) {
    apiResponseCounter.handleStatus(status);

    assertEquals(apiResponseCounter.informationalResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.successResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.redirectResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.clientErrorResponseCounter.count(), 0.0);
    assertEquals(apiResponseCounter.serverErrorResponseCounter.count(), 1.0);
  }

  @DataProvider(name = "information")
  public Object[][] information() {
    return new Object[][] {{100}, {101}};
  }

  @DataProvider(name = "success")
  public Object[][] success() {
    return new Object[][] {{200}, {201}, {202}, {203}, {204}};
  }

  @DataProvider(name = "redirect")
  public Object[][] redirect() {
    return new Object[][] {{300}, {301}, {302}, {303}, {304}};
  }

  @DataProvider(name = "clientError")
  public Object[][] clientError() {
    return new Object[][] {{400}, {401}, {402}, {403}, {404}, {405}};
  }

  @DataProvider(name = "serverError")
  public Object[][] serverError() {
    return new Object[][] {{500}, {501}, {502}, {503}, {504}};
  }
}
