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
package org.eclipse.che.commons.lang;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/** Test of ExpirableCache class */
public class ExpirableCacheTest {
    @Test
    public void shouldBeAbleToGetValueAfterPut() throws Exception {
        //given
        ExpirableCache<String, String> cache = new ExpirableCache<String, String>(500000, 100);
        cache.put("k1", "v1");
        //when
        String value = cache.get("k1");
        //then
        assertEquals(value, "v1");
        assertEquals(cache.getCacheSize(), 1);
    }

    @Test
    public void shouldNoBeAbleToGetValueMoreThenSize() throws Exception {
        //given
        ExpirableCache<String, String> cache = new ExpirableCache<String, String>(500000, 1);
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        //when
        String value = cache.get("k1");
        //then
        assertNull(value);
        assertEquals(cache.getCacheSize(), 1);
    }


    @Test
    public void shouldNoBeAbleToGetValueAfterInvalidationTime() throws Exception {
        //given
        ExpirableCache<String, String> cache = new ExpirableCache<String, String>(100, 1);
        cache.put("k1", "v1");
        Thread.sleep(200);
        //when
        String value = cache.get("k1");
        //then
        assertNull(value);
        assertEquals(cache.getCacheSize(), 0);
    }

    @Test
    public void shouldNoBeAbleToEvictValueAfter500AttemptsToGetOtherValue() throws Exception {
        //given
        ExpirableCache<String, String> cache = new ExpirableCache<String, String>(100, 1);
        cache.put("k1", "v1");
        Thread.sleep(200);
        //when
        int i = 0;
        while (i++ <= 500) {
            cache.get("k2");
        }
        //then
        assertEquals(cache.getCacheSize(), 0);
    }
}
