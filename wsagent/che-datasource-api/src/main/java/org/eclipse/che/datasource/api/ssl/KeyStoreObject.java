package org.eclipse.che.datasource.api.ssl;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.datasource.shared.ssl.SslKeyStoreEntry;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

/**
 * Created by test on 7/15/17.
 */
public class KeyStoreObject {
    public static final String SSL_KEY_STORE_PREF_ID = "sslKeyStore";

    public static final String TRUST_STORE_PREF_ID = "trustStore";

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreObject.class);

    protected String keyStorePassword;
    protected KeyStore keystore;

    protected String profileApiUrl;


    public KeyStoreObject() throws Exception {
        profileApiUrl = "http://192.168.1.35:8080" + "/profile";
        keystore = extractKeyStoreFromFile();
    }

    public KeyStoreObject(String apiUrl) throws Exception {
        profileApiUrl = apiUrl + "/profile";
        keystore = extractKeyStoreFromFile();
    }


    protected KeyStore extractKeyStoreFromFile() throws Exception {
        String sslKeyStore = getPreferences().get(getKeyStorePreferenceName());

        keyStorePassword = getKeyStorePassword();
        KeyStore ks = KeyStore.getInstance("JKS");

        if (sslKeyStore == null) {
            LOG.info("User KeyStore is null, creating a new one");
            ks.load(null, keyStorePassword.toCharArray());
            return ks;
        }
        try (InputStream fis = new ByteArrayInputStream(Base64.decodeBase64(sslKeyStore))) {
            ks.load(fis, keyStorePassword.toCharArray());
        } catch (Exception e) {
            LOG.info("Couldn't load keystore file ");
            ks.load(null, keyStorePassword.toCharArray());
        }

        return ks;
    }


    protected String getKeyStorePreferenceName() {
        return SSL_KEY_STORE_PREF_ID;
    }

//    private String getUserId() {
//        return EnvironmentContext.getCurrent().;
//    }

    protected String getKeyStorePassword() {
        return SslKeyStoreService.getDefaultKeystorePassword();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getKeyList(@Context UriInfo uriInfo) throws Exception {
        List<SslKeyStoreEntry> result = new ArrayList<SslKeyStoreEntry>(keystore.size());

        Enumeration<String> e = keystore.aliases();
        while (e.hasMoreElements()) {
            String alias = e.nextElement();
            SslKeyStoreEntry sslKeyStoreEntry = DtoFactory.getInstance().createDto(SslKeyStoreEntry.class)
                    .withAlias(alias)
                    .withType(
                            keystore.isKeyEntry(alias) ? "Key" : "Certificate"
                    );
            result.add(sslKeyStoreEntry);
        }
        return Response.ok().entity(result).type(MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("add")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response addNewKeyCertificateAndRespond(@QueryParam("alias") String alias,
                                                   Iterator<FileItem> uploadedFilesIterator) throws Exception {
        addNewKey(alias, uploadedFilesIterator);
        return Response.ok("", MediaType.TEXT_HTML).build();
    }

    public void addNewKey(String alias, Iterator<FileItem> uploadedFilesIterator) throws Exception {
        PrivateKey privateKey = null;
        Certificate[] certs = null;
        while (uploadedFilesIterator.hasNext()) {
            FileItem fileItem = uploadedFilesIterator.next();
            if (!fileItem.isFormField()) {
                if ("keyFile".equals(fileItem.getFieldName())) {
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(fileItem.get()));
                }
                if ("certFile".equals(fileItem.getFieldName())) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    certs = cf.generateCertificates(fileItem.getInputStream()).toArray(new Certificate[]{});
                }
            }
        }

        if (privateKey == null || certs == null) {
            throw new WebApplicationException(Response.ok("<pre>Can't find input file.</pre>", MediaType.TEXT_HTML).build());
        }

        keystore.setKeyEntry(alias, privateKey, keyStorePassword.toCharArray(), certs);
        save();
    }


    @GET
    @Path("{alias}/remove")
    public String deleteKeyWithGetMethod(@PathParam("alias") String alias, @QueryParam("callback") String calback) throws Exception {
        return deleteKey(alias, calback);
    }

    @DELETE
    @Path("{alias}")
    public String deleteKey(@PathParam("alias") String alias, @QueryParam("callback") String calback) throws Exception {
        keystore.deleteEntry(alias);
        save();
        return calback + "();";
    }

    protected void save() throws Exception {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        keystore.store(ostream, keyStorePassword.toCharArray());

        final Map<String, String> preferencesUpdate = new HashMap<>(4);
        preferencesUpdate.put(getKeyStorePreferenceName(), new String(Base64.encodeBase64(ostream.toByteArray())));
        HttpJsonHelper.post(null, profileApiUrl + "/prefs", preferencesUpdate);

    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getPreferences() throws IOException, ApiException {
        final String preferencesJson = HttpJsonHelper.requestString(profileApiUrl + "/prefs", "GET", null);
        try {
            return JsonHelper.fromJson(preferencesJson, Map.class, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (JsonParseException e) {
            throw new ApiException("It is not possible to get user preferences");
        }
    }
}
