/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor.signature;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureInfo;
import org.eclipse.lsp4j.SignatureInformation;

/** @author Evgen Vidolob */
class SignatureHelpImpl implements SignatureHelp {

  private final List<SignatureInfo> signatureInfos;
  private final Optional<Integer> activeSignature;
  private final Optional<Integer> activeParameter;

  public SignatureHelpImpl(org.eclipse.lsp4j.SignatureHelp dto) {
    activeParameter = Optional.fromNullable(dto.getActiveParameter());
    activeSignature = Optional.fromNullable(dto.getActiveSignature());
    signatureInfos = new ArrayList<>(dto.getSignatures().size());
    for (SignatureInformation SignatureInformation : dto.getSignatures()) {
      signatureInfos.add(new SignatureInfoImpl(SignatureInformation));
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
