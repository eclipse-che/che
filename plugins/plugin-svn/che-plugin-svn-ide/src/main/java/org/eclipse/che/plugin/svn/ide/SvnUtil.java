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
package org.eclipse.che.plugin.svn.ide;

import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.svn.shared.SubversionTypeConstant;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utility class for svn based operations.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
public class SvnUtil {

    /**
     * Checks whether the given {@code project} is under svn version control system.
     *
     * @param project
     *         the project to check
     * @return true if project is under svn, otherwise false
     */
    public static boolean isUnderSvn(Project project) {
        checkArgument(project != null, "Null project occurred");

        final List<String> mixins = project.getMixins();

        return mixins != null && mixins.contains(SubversionTypeConstant.SUBVERSION_MIXIN_TYPE);
    }

}
