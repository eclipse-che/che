/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server.compose;

import org.eclipse.che.api.environment.server.compose.model.ComposeEnvironmentImpl;
import org.eclipse.che.api.environment.server.compose.model.ComposeServiceImpl;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

/**
 * @author Alexander Garagatyi
 */
public class ComposeServicesStartStrategyTest {
    ComposeServicesStartStrategy strategy = new ComposeServicesStartStrategy();

    @Test
    public void shouldOrderServicesWithDependenciesWhereOrderIsStrict() throws Exception {
        ComposeEnvironmentImpl composeEnvironment = new ComposeEnvironmentImpl();
        composeEnvironment.getServices().put("second", new ComposeServiceImpl().withDependsOn(singletonList("first")));
        composeEnvironment.getServices().put("third", new ComposeServiceImpl().withDependsOn(asList("first", "second")));
        composeEnvironment.getServices().put("first", new ComposeServiceImpl().withDependsOn(emptyList()));
        composeEnvironment.getServices().put("forth", new ComposeServiceImpl().withDependsOn(singletonList("third")));
        composeEnvironment.getServices().put("fifth", new ComposeServiceImpl().withDependsOn(asList("forth", "first")));
        List<String> expected = asList("first", "second", "third", "forth", "fifth");

        List<String> actual = strategy.order(composeEnvironment);

        assertEquals(actual, expected);
    }

    @Test
    public void testOrderingOfServicesWithoutDependencies() throws Exception {
        ComposeEnvironmentImpl composeEnvironment = new ComposeEnvironmentImpl();
        composeEnvironment.getServices().put("second", new ComposeServiceImpl());
        composeEnvironment.getServices().put("third", new ComposeServiceImpl());
        composeEnvironment.getServices().put("first", new ComposeServiceImpl());
        String[] expected = new String[] {"first", "second", "third"};

        String[] actual =
                strategy.order(composeEnvironment).toArray(new String[composeEnvironment.getServices().size()]);

        assertEqualsNoOrder(actual, expected);
    }
}
