/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server.compose;

import org.eclipse.che.api.environment.server.DefaultServicesStartStrategy;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DefaultServicesStartStrategyTest {
    DefaultServicesStartStrategy strategy = new DefaultServicesStartStrategy();

    @Test
    public void shouldOrderServicesWithDependenciesWhereOrderIsStrict() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withDependsOn(singletonList("first")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withDependsOn(asList("first", "second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl().withDependsOn(emptyList()));
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withDependsOn(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withDependsOn(asList("forth", "first")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithDependenciesWhereOrderIsStrict2() {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("web", new CheServiceImpl().withDependsOn(asList("db", "redis")));
        composeEnvironment.getServices().put("redis", new CheServiceImpl().withDependsOn(singletonList("dev-machine")));
        composeEnvironment.getServices().put("db", new CheServiceImpl().withDependsOn(singletonList("redis")));
        composeEnvironment.getServices().put("dev-machine", new CheServiceImpl().withDependsOn(emptyList()));

        List<String> expected = asList("dev-machine", "redis", "db", "web");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void testOrderingOfServicesWithoutDependencies() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl());
        composeEnvironment.getServices().put("third", new CheServiceImpl());
        composeEnvironment.getServices().put("first", new CheServiceImpl());
        String[] expected = new String[] {"first", "second", "third"};

        // when
        String[] actual = strategy.order(composeEnvironment)
                                  .toArray(new String[composeEnvironment.getServices().size()]);

        // then
        assertEqualsNoOrder(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithDependenciesWhereOrderIsNotStrict() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withDependsOn(singletonList("first")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withDependsOn(singletonList("second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl().withDependsOn(emptyList()));
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withDependsOn(singletonList("second")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withDependsOn(singletonList("second")));

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual.get(0), "first");
        assertEquals(actual.get(1), "second");
        assertTrue(actual.contains("third"));
        assertTrue(actual.contains("forth"));
        assertTrue(actual.contains("fifth"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Launch order of machines '.*, .*' can't be evaluated. Circular dependency.")
    public void shouldFailIfCircularDependencyFound() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withDependsOn(singletonList("third")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withDependsOn(singletonList("second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "A machine can not link to itself: .*")
    public void shouldFailIfMachineLinksByItSelf() {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("first", new CheServiceImpl().withLinks(singletonList("first")));

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "A machine can not depend on itself: .*")
    public void shouldFailIfMachineDependsOnByItSelf() {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("first", new CheServiceImpl().withDependsOn(singletonList("first")));

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "A machine can not contain 'volumes_from' to itself:.*")
    public void shouldFailIfMachineContainsVolumesFromByItSelf() {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("first", new CheServiceImpl().withVolumesFrom(singletonList("first")));

        // when
        strategy.order(composeEnvironment);
    }

    @Test
    public void shouldOrderServicesWithLinks() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withLinks(singletonList("first:alias")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withLinks(asList("first", "second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl().withLinks(emptyList()));
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withLinks(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withLinks(asList("forth:alias", "first:alias")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithVolumesFrom() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withVolumesFrom(singletonList("first")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withVolumesFrom(asList("first", "second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl().withVolumesFrom(emptyList()));
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withVolumesFrom(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withVolumesFrom(asList("forth", "first")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithMixedDependenciesInDependsOnVolumesFromAndLinks() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withDependsOn(singletonList("first")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withVolumesFrom(asList("first", "second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl().withLinks(emptyList()));
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withLinks(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withDependsOn(asList("forth", "first")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithTheSameDependenciesInDependsOnVolumesFromAndLinks() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withVolumesFrom(singletonList("first"))
                                                                           .withDependsOn(singletonList("first"))
                                                                           .withLinks(singletonList("first:alias")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withVolumesFrom(asList("first", "second"))
                                                                          .withDependsOn(asList("first", "second"))
                                                                          .withLinks(asList("first", "second")));
        composeEnvironment.getServices().put("first", new CheServiceImpl());
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withVolumesFrom(singletonList("third"))
                                                                          .withDependsOn(singletonList("third"))
                                                                          .withLinks(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withVolumesFrom(asList("forth", "first"))
                                                                          .withDependsOn(asList("forth", "first"))
                                                                          .withLinks(asList("forth:alias", "first")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldOrderServicesWithComplementaryDependenciesInDependsOnLinksAndVolumesFrom() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withVolumesFrom(singletonList("first")));
        composeEnvironment.getServices().put("third", new CheServiceImpl().withVolumesFrom(singletonList("second"))
                                                                          .withDependsOn(singletonList("first")));
        composeEnvironment.getServices().put("first", new CheServiceImpl());
        composeEnvironment.getServices().put("forth", new CheServiceImpl().withVolumesFrom(singletonList("third"))
                                                                          .withDependsOn(singletonList("second"))
                                                                          .withLinks(singletonList("first:alias")));
        composeEnvironment.getServices().put("fifth", new CheServiceImpl().withVolumesFrom(singletonList("first"))
                                                                          .withLinks(singletonList("forth"))
                                                                          .withDependsOn(singletonList("second")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        // when
        List<String> actual = strategy.order(composeEnvironment);

        // then
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Dependency 'fifth' in machine 'second' points to unknown machine.")
    public void shouldFailIfDependsOnFieldContainsNonExistingService() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withDependsOn(singletonList("fifth")));
        composeEnvironment.getServices().put("third", new CheServiceImpl());
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Dependency 'fifth' in machine 'third' points to unknown machine.")
    public void shouldFailIfVolumesFromFieldContainsNonExistingService() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl());
        composeEnvironment.getServices().put("third", new CheServiceImpl().withVolumesFrom(singletonList("fifth")));
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Service volumes_from '.*' is invalid")
    public void shouldFailIfVolumesFromFieldHasIllegalFormat() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices()
                          .put("second",
                               new CheServiceImpl().withVolumesFrom(singletonList("first:broken:dependency")));
        composeEnvironment.getServices().put("third", new CheServiceImpl());
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Dependency 'fifth' in machine 'second' points to unknown machine.")
    public void shouldFailIfLinksFieldContainsNonExistingService() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices().put("second", new CheServiceImpl().withLinks(singletonList("fifth")));
        composeEnvironment.getServices().put("third", new CheServiceImpl());
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Service link '.*' is invalid")
    public void shouldFailIfLinksFieldHasIllegalFormat() throws Exception {
        // given
        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        composeEnvironment.getServices()
                          .put("second",
                               new CheServiceImpl().withLinks(singletonList("first:broken:dependency")));
        composeEnvironment.getServices().put("third", new CheServiceImpl());
        composeEnvironment.getServices().put("first", new CheServiceImpl());

        // when
        strategy.order(composeEnvironment);
    }
}
