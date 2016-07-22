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
package org.eclipse.che.maven.plugin;

import org.apache.maven.model.Dependency;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Check comparator of dependencies is working as expected
 * Sort first groupId, then artifactId, then version
 * @author Florent Benoit
 */
public class DependencyComparatorTest {


    /**
     * Check order after sorting is correct.
     */
    @Test
    public void checkOrder() {
        Dependency dependency1 = new Dependency();
        dependency1.setGroupId("b");
        dependency1.setArtifactId("a");
        dependency1.setVersion("1");

        Dependency dependency2 = new Dependency();
        dependency2.setGroupId("a");
        dependency2.setArtifactId("a");
        dependency2.setVersion("1");

        Dependency dependency3 = new Dependency();
        dependency3.setGroupId("a");
        dependency3.setArtifactId("b");
        dependency3.setVersion("1");

        Dependency dependency4 = new Dependency();
        dependency4.setGroupId("a");
        dependency4.setArtifactId("b");
        dependency4.setVersion("2");

        // add dependencies
        List<Dependency> dependencyList = Arrays.asList(dependency1, dependency2, dependency3, dependency4);

        // sort
        Collections.sort(dependencyList, new DependencyComparator());

        // test that sorting order is correct
        Assert.assertEquals(dependency2, dependencyList.get(0));
        Assert.assertEquals(dependency3, dependencyList.get(1));
        Assert.assertEquals(dependency4, dependencyList.get(2));
        Assert.assertEquals(dependency1, dependencyList.get(3));

    }

}
