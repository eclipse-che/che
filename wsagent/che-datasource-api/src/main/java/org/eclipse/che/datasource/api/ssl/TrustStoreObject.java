package org.eclipse.che.datasource.api.ssl;

import org.apache.commons.fileupload.FileItem;

import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Iterator;

/**
 * Created by test on 7/15/17.
 */
public class TrustStoreObject extends KeyStoreObject {

    public TrustStoreObject() throws Exception {
        super("http://192.168.1.35:8080");
    }


    @Override
    protected String getKeyStorePassword() {
        return SslKeyStoreService.getDefaultTrustorePassword();
    }

    @Override
    protected String getKeyStorePreferenceName() {
        return TRUST_STORE_PREF_ID;
    }

    @Override
    public Response addNewKeyCertificateAndRespond(@QueryParam("alias") String alias,
                                                   Iterator<FileItem> uploadedFilesIterator) throws Exception {
        addNewServerCACert(alias, uploadedFilesIterator);
        return Response.ok("", MediaType.TEXT_HTML).build();
    }

    public void addNewServerCACert(String alias, Iterator<FileItem> uploadedFilesIterator) throws Exception {
        Certificate[] certs = null;
        while (uploadedFilesIterator.hasNext()) {
            FileItem fileItem = uploadedFilesIterator.next();
            if (!fileItem.isFormField()) {
                if ("certFile".equals(fileItem.getFieldName())) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    certs = cf.generateCertificates(fileItem.getInputStream()).toArray(new Certificate[]{});
                }
            }
        }

        if (certs == null) {
            throw new WebApplicationException(Response.ok("<pre>Can't find input file.</pre>", MediaType.TEXT_HTML).build());
        }

        keystore.setCertificateEntry(alias, certs[0]);
        save();
    }
}
