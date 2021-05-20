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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EntryPointParserTest {

  @Test
  public void shouldParseCommandAndArgFromYAMLList() throws Exception {
    Map<String, String> cfg = new HashMap<>();
    cfg.put(MachineConfig.CONTAINER_COMMAND_ATTRIBUTE, "['/bin/sh', '''yaml quoting ftw''']");
    cfg.put(MachineConfig.CONTAINER_ARGS_ATTRIBUTE, "['x', 'y', '''z']");

    EntryPointParser parser = new EntryPointParser();

    EntryPoint ep = parser.parse(cfg);

    assertEquals(asList("/bin/sh", "'yaml quoting ftw'"), ep.getCommand());
    assertEquals(asList("x", "y", "'z"), ep.getArguments());
  }

  @Test
  public void shouldParseCommandFromYAMLList() throws Exception {
    Map<String, String> cfg = new HashMap<>();
    cfg.put(MachineConfig.CONTAINER_COMMAND_ATTRIBUTE, "['/bin/sh', '''yaml quoting ftw''']");

    EntryPointParser parser = new EntryPointParser();

    EntryPoint ep = parser.parse(cfg);

    assertEquals(asList("/bin/sh", "'yaml quoting ftw'"), ep.getCommand());
    assertEquals(emptyList(), ep.getArguments());
  }

  @Test
  public void shouldParseArgsFromYAMLList() throws Exception {
    Map<String, String> cfg = new HashMap<>();
    cfg.put(MachineConfig.CONTAINER_ARGS_ATTRIBUTE, "['x', 'y', '''z', --yes]");

    EntryPointParser parser = new EntryPointParser();

    EntryPoint ep = parser.parse(cfg);

    assertEquals(emptyList(), ep.getCommand());
    assertEquals(asList("x", "y", "'z", "--yes"), ep.getArguments());
  }

  @Test(dataProvider = "invalidEntryProvider", expectedExceptions = InfrastructureException.class)
  public void shouldFailOnOtherYAMLDataType(String invalidEntry) throws InfrastructureException {
    Map<String, String> cfg = new HashMap<>();
    cfg.put(MachineConfig.CONTAINER_ARGS_ATTRIBUTE, invalidEntry);

    EntryPointParser parser = new EntryPointParser();
    parser.parse(cfg);
  }

  @DataProvider
  public static Object[][] invalidEntryProvider() {
    return new Object[][] {
      new String[] {"key: value"},
      new String[] {"42"},
      new String[] {"true"},
      new String[] {"string value"},
      new String[] {"[a, b, [c]]"}
    };
  }

  @Test
  public void shouldSerializeValidData() {
    // given
    List<String> data = asList("/bin/sh", "-c");

    EntryPointParser parser = new EntryPointParser();

    // when
    String serialized = parser.serializeEntry(data);

    // then

    // this is dependent on the configuration of the YAML generator used by the YAMLMapper used in
    // the EntryPointParser so this may start failing on jackson-dataformat-yaml library upgrade
    assertEquals(serialized, "---\n- \"/bin/sh\"\n- \"-c\"\n");
  }
}
