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

import org.apache.maven.model.Plugin;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Check comparator of plugins is working as expected
 * Sort first groupId, then artifactId, then version
 * @author Florent Benoit
 */
public class PluginComparatorTest {


    /**
     * Check order after sorting is correct.
     */
    @Test
    public void checkOrder() {
        Plugin plugin1 = new Plugin();
        plugin1.setGroupId("b");
        plugin1.setArtifactId("a");
        plugin1.setVersion("1");

        Plugin plugin2 = new Plugin();
        plugin2.setGroupId("a");
        plugin2.setArtifactId("a");
        plugin2.setVersion("1");

        Plugin plugin3 = new Plugin();
        plugin3.setGroupId("a");
        plugin3.setArtifactId("b");
        plugin3.setVersion("1");

        Plugin plugin4 = new Plugin();
        plugin4.setGroupId("a");
        plugin4.setArtifactId("b");
        plugin4.setVersion("2");

        // add dependencies
        List<Plugin> pluginList = Arrays.asList(plugin1, plugin2, plugin3, plugin4);

        // sort
        Collections.sort(pluginList, new PluginComparator());

        // test that sorting order is correct
        Assert.assertEquals(plugin2, pluginList.get(0));
        Assert.assertEquals(plugin3, pluginList.get(1));
        Assert.assertEquals(plugin4, pluginList.get(2));
        Assert.assertEquals(plugin1, pluginList.get(3));

    }

}
