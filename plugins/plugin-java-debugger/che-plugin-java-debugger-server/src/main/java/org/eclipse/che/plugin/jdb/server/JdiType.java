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
package org.eclipse.che.plugin.jdb.server;

/** @author andrew00x */
public class JdiType {
    private JdiType() {
    }

    /**
     * <table>
     * <tr><th>Type Signature</th><th>Java Type</th></tr>
     * <tr>Z<td></td><td>boolean</td></tr>
     * <tr>B<td></td><td>byte</td></tr>
     * <tr>C<td></td><td>char</td></tr>
     * <tr>S<td></td><td>short</td></tr>
     * <tr>I<td></td><td>int</td></tr>
     * <tr>J<td></td><td>long</td></tr>
     * <tr>F<td></td><td>float</td></tr>
     * <tr>D<td></td><td>double</td></tr>
     * </table>
     *
     * @param signature
     *         variable signature
     * @return <code>true</code> if primitive and <code>false</code> otherwise
     */
    public static boolean isPrimitive(String signature) {
        char t = signature.charAt(0);
        return t == 'Z' || t == 'B' || t == 'C' || t == 'S' || t == 'I' || t == 'J' || t == 'F' || t == 'D';
    }

    /**
     * @param signature
     *         variable signature
     * @return <code>true</code> if array and <code>false</code> otherwise
     */
    public static boolean isArray(String signature) {
        return signature.charAt(0) == '[';
    }
}
