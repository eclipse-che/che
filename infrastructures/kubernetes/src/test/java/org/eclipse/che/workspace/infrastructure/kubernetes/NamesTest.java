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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.util.Map;
import org.testng.annotations.Test;

public class NamesTest {

  @Test
  public void shouldStartIndexingMachineNamesAtOne() {
    // given
    ObjectMeta metaData = new ObjectMeta();

    // when
    Names.putMachineName(metaData, "container", "machine");

    // then
    Map<String, String> annos = metaData.getAnnotations();

    assertEquals(2, annos.size());
    assertEquals("container", annos.get("che.container.1.name"));
    assertEquals("machine", annos.get("che.container.1.machine"));
  }

  @Test
  public void shouldIncreaseMachineIndexByOneOnInsertion() {
    // given
    ObjectMeta metaData = new ObjectMeta();

    // when
    Names.putMachineName(metaData, "container1", "machine1");
    Names.putMachineName(metaData, "container2", "machine2");

    // then
    Map<String, String> annos = metaData.getAnnotations();

    assertEquals(4, annos.size());
    assertEquals("container1", annos.get("che.container.1.name"));
    assertEquals("machine1", annos.get("che.container.1.machine"));
    assertEquals("container2", annos.get("che.container.2.name"));
    assertEquals("machine2", annos.get("che.container.2.machine"));
  }

  @Test
  public void shouldReadMachineName() {
    // given
    ObjectMeta metaData = new ObjectMeta();
    Container container1 = new Container();
    container1.setName("container1");
    Container container2 = new Container();
    container2.setName("container2");

    // when
    Names.putMachineName(metaData, container1.getName(), "machine1");
    Names.putMachineName(metaData, container2.getName(), "machine2");

    // then
    assertEquals(Names.machineName(metaData, container1), "machine1");
    assertEquals(Names.machineName(metaData, container2), "machine2");
  }
}
