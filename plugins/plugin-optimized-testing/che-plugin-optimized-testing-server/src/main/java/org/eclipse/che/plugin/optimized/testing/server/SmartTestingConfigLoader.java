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

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.arquillian.smart.testing.configuration.Configuration;
import org.arquillian.smart.testing.configuration.ConfigurationLoader;
import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.dto.SmartTestingConfigurationDto;
import org.eclipse.che.api.testing.shared.dto.SmartTestingConfigurationParamDto;
import org.eclipse.che.dto.server.DtoFactory;

class SmartTestingConfigLoader {

  static Configuration load(String projectPath) {
    File projectDir = new File(Paths.get("").toAbsolutePath() + File.separator + projectPath);
    return ConfigurationLoader.load(projectDir, file -> false);
  }

  static SmartTestingConfigurationDto getMainSmartTestingConfigParams(String projectPath) {
    Configuration configuration = load(projectPath);

    SmartTestingConfigurationDto configDto =
        DtoFactory.getInstance().createDto(SmartTestingConfigurationDto.class);
    Map<String, SmartTestingConfigurationParamDto> params = new HashMap<>();
    addConfigParamDto(params, OptimizedConstants.STRATEGIES, configuration.getStrategies());
    addConfigParamDto(params, OptimizedConstants.MODE, configuration.getMode().getName());

    configDto.setParameters(params);
    return configDto;
  }

  private static void addConfigParamDto(
      Map<String, SmartTestingConfigurationParamDto> params, String name, String... values) {
    params.put(name, createConfigParamDto(name, values));
  }

  private static SmartTestingConfigurationParamDto createConfigParamDto(
      String name, String... values) {
    SmartTestingConfigurationParamDto dtoParam =
        DtoFactory.getInstance().createDto(SmartTestingConfigurationParamDto.class);
    dtoParam.setName(name);
    dtoParam.setValues(Arrays.asList(values));
    return dtoParam;
  }
}
