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
package org.eclipse.che.plugin.pullrequest.client;

import org.eclipse.che.ide.api.extension.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Registers event handlers for adding/removing contribution part.
 *
 * <p>Manages {@code AppContext#getRootProject}
 * current root project state, in the case of adding and removing 'contribution' mixin.
 * Contribution mixin itself is 'synthetic' one and needed only for managing plugin specific project attributes.
 *
 * @author Stephane Tournie
 * @author Kevin Pollet
 * @author Yevhenii Voevodin
 */
@Singleton
@Extension(title = "Contributor", version = "1.0.0")
public class ContributionExtension {

    @Inject
    @SuppressWarnings("unused")
    public ContributionExtension(ContributeResources resources,
                                 ContributionMixinProvider contributionMixinProvider) {
        resources.contributeCss().ensureInjected();
    }
}
