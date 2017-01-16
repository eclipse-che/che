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
package org.eclipse.che.commons.json;


import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JsonTest {
    public static class Foo {
        private String fooBar;

        public String getFooBar() {
            return fooBar;
        }

        public void setFooBar(String fooBar) {
            this.fooBar = fooBar;
        }
    }

    @Test
    public void testSerializeDefault() throws Exception {
        String expectedJson = "{\"fooBar\":\"test\"}";
        Foo foo = new Foo();
        foo.setFooBar("test");
        assertEquals(expectedJson, JsonHelper.toJson(foo));
    }

    @Test
    public void testSerializeUnderscore() throws Exception {
        String expectedJson = "{\"foo_bar\":\"test\"}";
        Foo foo = new Foo();
        foo.setFooBar("test");
        assertEquals(expectedJson, JsonHelper.toJson(foo, JsonNameConventions.CAMEL_UNDERSCORE));
    }

    @Test
    public void testSerializeDash() throws Exception {
        String expectedJson = "{\"foo-bar\":\"test\"}";
        Foo foo = new Foo();
        foo.setFooBar("test");
        assertEquals(expectedJson, JsonHelper.toJson(foo, JsonNameConventions.CAMEL_DASH));
    }

    @Test
    public void testDeserializeDefault() throws Exception {
        String json = "{\"fooBar\":\"test\"}";
        Foo foo = JsonHelper.fromJson(json, Foo.class, null);
        assertEquals("test", foo.getFooBar());
    }

    @Test
    public void testDeserializeUnderscore() throws Exception {
        String json = "{\"foo_bar\":\"test\"}";
        Foo foo = JsonHelper.fromJson(json, Foo.class, null, JsonNameConventions.CAMEL_UNDERSCORE);
        assertEquals("test", foo.getFooBar());
    }

    @Test
    public void testDeserializeDash() throws Exception {
        String json = "{\"foo-bar\":\"test\"}";
        Foo foo = JsonHelper.fromJson(json, Foo.class, null, JsonNameConventions.CAMEL_DASH);
        assertEquals("test", foo.getFooBar());
    }
}
