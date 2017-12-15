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
package org.eclipse.che.plugin.optimized.testing.server;

import static java.util.Arrays.asList;
import static org.arquillian.smart.testing.RunMode.ORDERING;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.REPORT_FILE_NAME;
import static org.arquillian.smart.testing.report.SmartTestingReportGenerator.TARGET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.che.ide.rest.HTTPMethod.HEAD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.Range;
import org.arquillian.smart.testing.configuration.Report;
import org.arquillian.smart.testing.configuration.Scm;
import org.junit.Test;

public class SmartTestingConfigLoaderTest {

  @Test
  public void should_load_configuration_file() throws IOException {
    // given
    Configuration expectedConfiguration = createExpectedConfigurationInstance();

    // when
    final Configuration actualConfiguration =
        SmartTestingConfigLoader.load("/src/test/resources/configuration/");

    // then
    assertThat(actualConfiguration)
        .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
  }

  @Test
  public void should_recursively_find_and_load_configuration_file() throws IOException {
    // given
    Configuration expectedConfiguration = createExpectedConfigurationInstance();

    // when
    final Configuration actualConfiguration =
        SmartTestingConfigLoader.load(
            "/src/test/resources/configuration/subdirectory/without/config");

    // then
    assertThat(actualConfiguration)
        .isEqualToComparingFieldByFieldRecursively(expectedConfiguration);
  }

  private Configuration createExpectedConfigurationInstance() {
    final Report report = new Report();
    report.setEnable(true);
    report.setDir(TARGET);
    report.setName(REPORT_FILE_NAME);

    final Range range = new Range();
    range.setHead(HEAD);
    range.setTail(HEAD + "~2");

    final Scm scm = new Scm();
    scm.setRange(range);

    Map<String, Object> affectedStrategiesConfig = new HashMap<>();
    affectedStrategiesConfig.put("exclusions", asList("org.package.*", "org.arquillian.package.*"));
    affectedStrategiesConfig.put(
        "inclusions", asList("org.package.exclude.*", "org.arquillian.package.exclude.*"));
    affectedStrategiesConfig.put("transitivity", true);

    Map<String, Object> strategiesConfig = new HashMap<>();
    strategiesConfig.put("affected", affectedStrategiesConfig);

    final Configuration expectedConfiguration = new Configuration();
    expectedConfiguration.setMode(ORDERING);
    expectedConfiguration.setStrategies("new", "changed", "affected");
    expectedConfiguration.setApplyTo("surefire");
    expectedConfiguration.setDebug(true);
    expectedConfiguration.setDisable(false);
    expectedConfiguration.setScm(scm);
    expectedConfiguration.setReport(report);
    expectedConfiguration.setAutocorrect(true);
    expectedConfiguration.setStrategiesConfig(strategiesConfig);
    expectedConfiguration.setCustomStrategies(
        new String[] {
          "smart.testing.strategy.cool=org.arquillian.smart.testing:strategy-cool:1.0.0",
          "smart.testing.strategy.experimental=org.arquillian.smart.testing:strategy-experimental:1.0.0"
        });

    return expectedConfiguration;
  }
}
