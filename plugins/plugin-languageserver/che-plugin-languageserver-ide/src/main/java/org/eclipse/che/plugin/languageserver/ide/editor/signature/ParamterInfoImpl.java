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
package org.eclipse.che.plugin.languageserver.ide.editor.signature;

import com.google.common.base.Optional;

import org.eclipse.che.api.languageserver.shared.lsapi.ParameterInformationDTO;
import org.eclipse.che.ide.api.editor.signature.ParameterInfo;

/**
 * @author Evgen Vidolob
 */
class ParamterInfoImpl implements ParameterInfo {

    private final ParameterInformationDTO dto;

    public ParamterInfoImpl(ParameterInformationDTO dto) {
        this.dto = dto;
    }

    @Override
    public String getLabel() {
        return dto.getLabel();
    }

    @Override
    public Optional<String> getDocumentation() {
        return Optional.fromNullable(dto.getDocumentation());
    }
}
