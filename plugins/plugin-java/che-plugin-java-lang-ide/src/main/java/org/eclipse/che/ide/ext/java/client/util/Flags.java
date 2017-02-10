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
package org.eclipse.che.ide.ext.java.client.util;

/**
 * Utility class for decoding modifier flags in Java elements.
 *
 * @author Evgen Vidolob
 */
public class Flags {

    private static final int AccDefault           = 0;
    private static final int AccDefaultMethod     = 0x10000;
    private static final int AccPublic            = 0x0001;
    private static final int AccPrivate           = 0x0002;
    private static final int AccProtected         = 0x0004;
    private static final int AccStatic            = 0x0008;
    private static final int AccFinal             = 0x0010;
    private static final int AccSynchronized      = 0x0020;
    private static final int AccVolatile          = 0x0040;
    private static final int AccBridge            = 0x0040;
    private static final int AccTransient         = 0x0080;
    private static final int AccVarargs           = 0x0080;
    private static final int AccNative            = 0x0100;
    private static final int AccInterface         = 0x0200;
    private static final int AccAbstract          = 0x0400;
    private static final int AccStrictfp          = 0x0800;
    private static final int AccSynthetic         = 0x1000;
    private static final int AccAnnotation        = 0x2000;
    private static final int AccEnum              = 0x4000;
    private static final int AccSuper             = 0x0020;
    private static final int AccMandated          = 0x8000;
    private static final int AccAnnotationDefault = 0x20000;
    private static final int AccDeprecated        = 0x100000;

    private Flags() {
        // Not instantiable
    }

    /**
     * Returns whether the given integer includes the <code>abstract</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>abstract</code> modifier is included
     */
    public static boolean isAbstract(int flags) {
        return (flags & AccAbstract) != 0;
    }

    /**
     * Returns whether the given integer includes the indication that the
     * element is deprecated (<code>@deprecated</code> tag in Javadoc comment).
     *
     * @param flags the flags
     * @return <code>true</code> if the element is marked as deprecated
     */
    public static boolean isDeprecated(int flags) {
        return (flags & AccDeprecated) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>final</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>final</code> modifier is included
     */
    public static boolean isFinal(int flags) {
        return (flags & AccFinal) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>interface</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>interface</code> modifier is included
     */
    public static boolean isInterface(int flags) {
        return (flags & AccInterface) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>native</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>native</code> modifier is included
     */
    public static boolean isNative(int flags) {
        return (flags & AccNative) != 0;
    }

    /**
     * Returns whether the given integer does not include one of the
     * <code>public</code>, <code>private</code>, or <code>protected</code> flags.
     *
     * @param flags the flags
     * @return <code>true</code> if no visibility flag is set
     */
    public static boolean isPackageDefault(int flags) {
        return (flags & (AccPublic | AccPrivate | AccProtected)) == 0;
    }

    /**
     * Returns whether the given integer includes the <code>private</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>private</code> modifier is included
     */
    public static boolean isPrivate(int flags) {
        return (flags & AccPrivate) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>protected</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>protected</code> modifier is included
     */
    public static boolean isProtected(int flags) {
        return (flags & AccProtected) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>public</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>public</code> modifier is included
     */
    public static boolean isPublic(int flags) {
        return (flags & AccPublic) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>static</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>static</code> modifier is included
     */
    public static boolean isStatic(int flags) {
        return (flags & AccStatic) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>super</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>super</code> modifier is included
     */
    public static boolean isSuper(int flags) {
        return (flags & AccSuper) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>strictfp</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>strictfp</code> modifier is included
     */
    public static boolean isStrictfp(int flags) {
        return (flags & AccStrictfp) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>synchronized</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>synchronized</code> modifier is included
     */
    public static boolean isSynchronized(int flags) {
        return (flags & AccSynchronized) != 0;
    }

    /**
     * Returns whether the given integer includes the indication that the
     * element is synthetic.
     *
     * @param flags the flags
     * @return <code>true</code> if the element is marked synthetic
     */
    public static boolean isSynthetic(int flags) {
        return (flags & AccSynthetic) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>transient</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>transient</code> modifier is included
     */
    public static boolean isTransient(int flags) {
        return (flags & AccTransient) != 0;
    }

    /**
     * Returns whether the given integer includes the <code>volatile</code> modifier.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>volatile</code> modifier is included
     */
    public static boolean isVolatile(int flags) {
        return (flags & AccVolatile) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccBridge</code>
     * bit set.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>AccBridge</code> flag is included
     */
    public static boolean isBridge(int flags) {
        return (flags & AccBridge) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccVarargs</code>
     * bit set.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>AccVarargs</code> flag is included
     */
    public static boolean isVarargs(int flags) {
        return (flags & AccVarargs) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccEnum</code>
     * bit set.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>AccEnum</code> flag is included
     */
    public static boolean isEnum(int flags) {
        return (flags & AccEnum) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccAnnotation</code>
     * bit set.
     *
     * @param flags the flags
     * @return <code>true</code> if the <code>AccAnnotation</code> flag is included
     */
    public static boolean isAnnotation(int flags) {
        return (flags & AccAnnotation) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccDefaultMethod</code>
     * bit set. Note that this flag represents the usage of the 'default' keyword
     * on a method and should not be confused with the 'package' access visibility (which used to be called 'default access').
     *
     * @return <code>true</code> if the <code>AccDefaultMethod</code> flag is included
     */
    public static boolean isDefaultMethod(int flags) {
        return (flags & AccDefaultMethod) != 0;
    }

    /**
     * Returns whether the given integer has the <code>AccAnnnotationDefault</code>
     * bit set.
     *
     * @return <code>true</code> if the <code>AccAnnotationDefault</code> flag is included
     */
    public static boolean isAnnnotationDefault(int flags) {
        return (flags & AccAnnotationDefault) != 0;
    }

}
