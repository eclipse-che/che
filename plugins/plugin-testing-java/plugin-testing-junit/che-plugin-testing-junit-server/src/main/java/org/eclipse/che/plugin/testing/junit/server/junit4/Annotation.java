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
package org.eclipse.che.plugin.testing.junit.server.junit4;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Describes JUnit test annotations {@code org.junit.runner.RunWith} and {@code org.junit.Test}.
 */
public class Annotation {
    public static final Annotation RUN_WITH = new Annotation("org.junit.runner.RunWith");
    public static final Annotation TEST     = new Annotation("org.junit.Test");

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

    /**
     * Check if type annotated as test type.
     *
     * @param type
     *         type which should be checked
     * @return {@code true} if type annotated as test otherwise returns {@code false}
     */
    public boolean annotatesTypeOrSuperTypes(ITypeBinding type) {
        while (type != null) {
            if (annotates(type.getAnnotations())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    /**
     * Find method which annotated as test method.
     *
     * @param type
     *         type which contains methods
     * @return {@code true} if least one has test annotation otherwise returns {@code false}
     */
    public boolean annotatesAtLeastOneMethod(ITypeBinding type) {
        while (type != null) {
            IMethodBinding[] declaredMethods = type.getDeclaredMethods();
            for (IMethodBinding declaredMethod : declaredMethods) {
                if (annotates(declaredMethod.getAnnotations())) {
                    return true;
                }
            }
            type = type.getSuperclass();
        }
        return false;
    }
}
