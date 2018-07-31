/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package renameMethodsInInterface.testGenerics01;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

interface I {
    Set<Set<Runnable>> zYXteg(List<Set<Runnable>> arg);
}

class Impl implements I {
    public Set<Set<Runnable>> zYXteg(List<Set<Runnable>> arg) {
    return null;
    }
}

class User {
    void call(I abc) {
    Set<Set<Runnable>> s= abc.zYXteg(new ArrayList<Set<Runnable>>());
    }
}
