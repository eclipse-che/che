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
package org.eclipse.che.api.factory.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Latest version of factory implementation.
 *
 * @author Alexander Garagatyi
 */
@DTO
public interface Factory extends FactoryV4_0, Hyperlinks {
    Factory withV(String v);

    Factory withId(String id);
    
    Factory withName(String name);

    Factory withWorkspace(WorkspaceConfigDto workspace);

    Factory withPolicies(Policies policies);

    Factory withCreator(Author creator);

    Factory withButton(Button button);

    Factory withIde(Ide ide);

    @Override
    Factory withLinks(List<Link> links);
}
