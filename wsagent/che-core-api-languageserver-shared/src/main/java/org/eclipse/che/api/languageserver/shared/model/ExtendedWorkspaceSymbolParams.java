package org.eclipse.che.api.languageserver.shared.model;

import org.eclipse.lsp4j.WorkspaceSymbolParams;

/**
 * Version of workspace symbol params that holds the uri of the file the ide has open.
 *
 * @author Thomas Mäder
 */
public class ExtendedWorkspaceSymbolParams extends WorkspaceSymbolParams {

    private String fileUri;

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
}
