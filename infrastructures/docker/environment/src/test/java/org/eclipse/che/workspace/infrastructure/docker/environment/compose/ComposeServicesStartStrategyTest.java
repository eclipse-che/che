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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.testng.annotations.Test;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ComposeServicesStartStrategyTest {
  private ComposeServicesStartStrategy strategy = new ComposeServicesStartStrategy();

  @Test
  public void shouldOrderServicesWithDependenciesWhereOrderIsStrict() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withDependsOn(singletonList("first")));
    services.put("third", new ComposeService().withDependsOn(asList("first", "second")));
    services.put("first", new ComposeService().withDependsOn(emptyList()));
    services.put("forth", new ComposeService().withDependsOn(singletonList("third")));
    services.put("fifth", new ComposeService().withDependsOn(asList("forth", "first")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void shouldOrderServicesWithDependenciesWhereOrderIsStrict2() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("web", new ComposeService().withDependsOn(asList("db", "redis")));
    services.put("redis", new ComposeService().withDependsOn(singletonList("dev-machine")));
    services.put("db", new ComposeService().withDependsOn(singletonList("redis")));
    services.put("dev-machine", new ComposeService().withDependsOn(emptyList()));

    String[] expected = new String[] {"dev-machine", "redis", "db", "web"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void testOrderingOfServicesWithoutDependencies() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService());
    services.put("third", new ComposeService());
    services.put("first", new ComposeService());
    String[] expected = new String[] {"first", "second", "third"};

    // when
    String[] actual = strategy.order(services).keySet().toArray(new String[services.size()]);

    // then
    assertEqualsNoOrder(actual, expected);
  }

  @Test
  public void shouldOrderServicesWithDependenciesWhereOrderIsNotStrict() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withDependsOn(singletonList("first")));
    services.put("third", new ComposeService().withDependsOn(singletonList("second")));
    services.put("first", new ComposeService().withDependsOn(emptyList()));
    services.put("forth", new ComposeService().withDependsOn(singletonList("second")));
    services.put("fifth", new ComposeService().withDependsOn(singletonList("second")));

    // when
    Set<String> actual = strategy.order(services).keySet();

    Iterator<String> iterator = actual.iterator();
    // then
    assertEquals(iterator.next(), "first");
    assertEquals(iterator.next(), "second");
    assertTrue(actual.contains("third"));
    assertTrue(actual.contains("forth"));
    assertTrue(actual.contains("fifth"));
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Launch order of services '.*, .*' can't be evaluated. Circular dependency."
  )
  public void shouldFailIfCircularDependencyFound() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withDependsOn(singletonList("third")));
    services.put("third", new ComposeService().withDependsOn(singletonList("second")));
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "A service can not link to itself: .*"
  )
  public void shouldFailIfMachineLinksByItSelf() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("first", new ComposeService().withLinks(singletonList("first")));

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "A service can not depend on itself: .*"
  )
  public void shouldFailIfMachineDependsOnByItSelf() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("first", new ComposeService().withDependsOn(singletonList("first")));

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "A service can not contain 'volumes_from' to itself:.*"
  )
  public void shouldFailIfMachineContainsVolumesFromByItSelf() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("first", new ComposeService().withVolumesFrom(singletonList("first")));

    // when
    strategy.order(services);
  }

  @Test
  public void shouldOrderServicesWithLinks() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withLinks(singletonList("first:alias")));
    services.put("third", new ComposeService().withLinks(asList("first", "second")));
    services.put("first", new ComposeService().withLinks(emptyList()));
    services.put("forth", new ComposeService().withLinks(singletonList("third")));
    services.put("fifth", new ComposeService().withLinks(asList("forth:alias", "first:alias")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void shouldOrderServicesWithVolumesFrom() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withVolumesFrom(singletonList("first")));
    services.put("third", new ComposeService().withVolumesFrom(asList("first", "second")));
    services.put("first", new ComposeService().withVolumesFrom(emptyList()));
    services.put("forth", new ComposeService().withVolumesFrom(singletonList("third")));
    services.put("fifth", new ComposeService().withVolumesFrom(asList("forth", "first")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void shouldOrderServicesWithMixedDependenciesInDependsOnVolumesFromAndLinks()
      throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withDependsOn(singletonList("first")));
    services.put("third", new ComposeService().withVolumesFrom(asList("first", "second")));
    services.put("first", new ComposeService().withLinks(emptyList()));
    services.put("forth", new ComposeService().withLinks(singletonList("third")));
    services.put("fifth", new ComposeService().withDependsOn(asList("forth", "first")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void shouldOrderServicesWithTheSameDependenciesInDependsOnVolumesFromAndLinks()
      throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put(
        "second",
        new ComposeService()
            .withVolumesFrom(singletonList("first"))
            .withDependsOn(singletonList("first"))
            .withLinks(singletonList("first:alias")));
    services.put(
        "third",
        new ComposeService()
            .withVolumesFrom(asList("first", "second"))
            .withDependsOn(asList("first", "second"))
            .withLinks(asList("first", "second")));
    services.put("first", new ComposeService());
    services.put(
        "forth",
        new ComposeService()
            .withVolumesFrom(singletonList("third"))
            .withDependsOn(singletonList("third"))
            .withLinks(singletonList("third")));
    services.put(
        "fifth",
        new ComposeService()
            .withVolumesFrom(asList("forth", "first"))
            .withDependsOn(asList("forth", "first"))
            .withLinks(asList("forth:alias", "first")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test
  public void shouldOrderServicesWithComplementaryDependenciesInDependsOnLinksAndVolumesFrom()
      throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withVolumesFrom(singletonList("first")));
    services.put(
        "third",
        new ComposeService()
            .withVolumesFrom(singletonList("second"))
            .withDependsOn(singletonList("first")));
    services.put("first", new ComposeService());
    services.put(
        "forth",
        new ComposeService()
            .withVolumesFrom(singletonList("third"))
            .withDependsOn(singletonList("second"))
            .withLinks(singletonList("first:alias")));
    services.put(
        "fifth",
        new ComposeService()
            .withVolumesFrom(singletonList("first"))
            .withLinks(singletonList("forth"))
            .withDependsOn(singletonList("second")));
    String[] expected = new String[] {"first", "second", "third", "forth", "fifth"};

    // when
    Map<String, ComposeService> actual = strategy.order(services);

    // then
    assertEquals(actual.keySet().toArray(), expected);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Dependency 'fifth' in service 'second' points to unknown service."
  )
  public void shouldFailIfDependsOnFieldContainsNonExistingService() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withDependsOn(singletonList("fifth")));
    services.put("third", new ComposeService());
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Dependency 'fifth' in service 'third' points to unknown service."
  )
  public void shouldFailIfVolumesFromFieldContainsNonExistingService() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService());
    services.put("third", new ComposeService().withVolumesFrom(singletonList("fifth")));
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Service volumes_from '.*' is invalid"
  )
  public void shouldFailIfVolumesFromFieldHasIllegalFormat() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put(
        "second", new ComposeService().withVolumesFrom(singletonList("first:broken:dependency")));
    services.put("third", new ComposeService());
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Dependency 'fifth' in service 'second' points to unknown service."
  )
  public void shouldFailIfLinksFieldContainsNonExistingService() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put("second", new ComposeService().withLinks(singletonList("fifth")));
    services.put("third", new ComposeService());
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Service link '.*' is invalid"
  )
  public void shouldFailIfLinksFieldHasIllegalFormat() throws Exception {
    // given
    Map<String, ComposeService> services = new HashMap<>();
    services.put(
        "second", new ComposeService().withLinks(singletonList("first:broken:dependency")));
    services.put("third", new ComposeService());
    services.put("first", new ComposeService());

    // when
    strategy.order(services);
  }
}
