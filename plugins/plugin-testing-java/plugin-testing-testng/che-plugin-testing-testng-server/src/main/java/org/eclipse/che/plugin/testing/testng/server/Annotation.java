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
package org.eclipse.che.plugin.testing.testng.server;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.testng.annotations.Test;

/**
 * Describes TestNG test annotations {@code org.testng.annotations.Test}.
 */
public class Annotation {
    public static final Annotation TEST = new Annotation(Test.class.getName());

    private final String fName;

    private Annotation(String name) {
        fName = name;
    }

    /**
     * Returns name of the annotation.
     *
     * @return name of the annotation
     */
    public String getName() {
        return fName;
    }

    private boolean annotates(IAnnotationBinding[] annotations) {
        for (IAnnotationBinding annotation : annotations) {
            ITypeBinding annotationType = annotation.getAnnotationType();
            if (annotationType != null && (annotationType.getQualifiedName().equals(fName))) {
                return true;
            }
        }
        return false;
    }
}
