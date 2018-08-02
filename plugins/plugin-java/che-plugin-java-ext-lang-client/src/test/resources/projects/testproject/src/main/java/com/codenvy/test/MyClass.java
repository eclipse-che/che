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
package org.eclipse.che.test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

public class MyClass<T> extends ArrayList<T> implements Closeable{

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean add(T t) {
        return super.add(t);
    }

    public int aadad(String parameterName){
        return 0;
    }
}
