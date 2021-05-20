/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.env;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EnvVarsConverterTest {

  private static final String PRE_EXISTING_VAR = "VAR_THAT_EXISTS";
  private static final String PRE_EXISTING_VAR_VALUE = "jmenuju se VAR";
  private static final String PRE_EXISTING_VAR_NEW_VALUE = "my name is VAR";
  private static final String A_VAR = "A";
  private static final String A_VAL = "$(C)";
  private static final String B_VAR = "B";
  private static final String B_VAL = "b";
  private static final String C_VAR = "C";
  private static final String C_VAL = "c";

  private KubernetesEnvironment environment;
  private RuntimeIdentity identity;
  private Container testContainer;
  private InternalMachineConfig machine;

  @BeforeMethod
  public void setUp() {
    testContainer = new Container();

    PodSpec podSpec = new PodSpec();
    podSpec.setContainers(singletonList(testContainer));

    ObjectMeta podMeta = new ObjectMeta();
    podMeta.setName("pod");

    Pod pod = new Pod();
    pod.setSpec(podSpec);
    pod.setMetadata(podMeta);

    Map<String, Pod> pods = new HashMap<>();
    pods.put("pod", pod);

    environment = KubernetesEnvironment.builder().setPods(pods).build();

    machine = new InternalMachineConfig();

    environment.setMachines(
        Collections.singletonMap(Names.machineName(podMeta, testContainer), machine));

    identity = new RuntimeIdentityImpl("wsId", "blah", "bleh", "infraNamespace");
  }

  @Test
  public void shouldProvisionEnvironmentVariablesSorted() throws InfrastructureException {
    // given
    List<EnvVar> preExistingEnvironment = new ArrayList<>();
    preExistingEnvironment.add(new EnvVar(PRE_EXISTING_VAR, PRE_EXISTING_VAR_VALUE, null));
    testContainer.setEnv(preExistingEnvironment);

    machine.getEnv().put(PRE_EXISTING_VAR, PRE_EXISTING_VAR_NEW_VALUE);
    machine.getEnv().put(A_VAR, A_VAL);
    machine.getEnv().put(B_VAR, B_VAL);
    machine.getEnv().put(C_VAR, C_VAL);

    // when
    EnvVarsConverter converter = new EnvVarsConverter();
    converter.provision(environment, identity);

    // then
    EnvVar expectedA = new EnvVar(A_VAR, A_VAL, null);
    EnvVar expectedB = new EnvVar(B_VAR, B_VAL, null);
    EnvVar expectedC = new EnvVar(C_VAR, C_VAL, null);
    EnvVar expectedPreExisting = new EnvVar(PRE_EXISTING_VAR, PRE_EXISTING_VAR_NEW_VALUE, null);

    List<EnvVar> expectedOrder = asList(expectedB, expectedC, expectedA, expectedPreExisting);

    assertEquals(4, testContainer.getEnv().size());
    assertEquals(expectedOrder, testContainer.getEnv());
  }
}
