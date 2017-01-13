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
package org.eclipse.che.plugin.languageserver.ide.editor.signature;

import com.google.common.base.Optional;

import org.eclipse.che.api.languageserver.shared.lsapi.SignatureHelpDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.SignatureInformationDTO;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
class SignatureHelpImpl implements SignatureHelp {

    private final List<SignatureInfo> signatureInfos;
    private final Optional<Integer>   activeSignature;
    private final Optional<Integer>   activeParameter;

    public SignatureHelpImpl(SignatureHelpDTO dto) {
        activeParameter = Optional.fromNullable(dto.getActiveParameter());
        activeSignature = Optional.fromNullable(dto.getActiveSignature());
        signatureInfos = new ArrayList<>(dto.getSignatures().size());
        for (SignatureInformationDTO signatureInformationDTO : dto.getSignatures()) {
            signatureInfos.add(new SignatureInfoImpl(signatureInformationDTO));
        }
    }

    @Override
    public List<SignatureInfo> getSignatures() {
        return signatureInfos;
    }

    @Override
    public Optional<Integer> getActiveSignature() {
        return activeSignature;
    }

    @Override
    public Optional<Integer> getActiveParameter() {
        return activeParameter;
    }
}
