package org.eclipse.che.datasource.api;

import org.eclipse.che.datasource.shared.ServicePaths;
import org.eclipse.che.dto.server.DtoFactory;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.che.datasource.shared.TextDto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by test on 7/15/17.
 */
@Path(ServicePaths.ENCRYPT_TEXT_PATH)
public class EncryptTextService {
    private static final byte[] SALT = {
            (byte)0xde, (byte)0x33, (byte)0x10, (byte)0x12,
            (byte)0xde, (byte)0x33, (byte)0x10, (byte)0x12,
    };
    /** The logger. */
    private static final Logger LOG  = LoggerFactory.getLogger(EncryptTextService.class);

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public String encryptText(TextDto textToEncrypt) throws Exception {
        String encryptedText = encrypt(textToEncrypt.getValue());
        TextDto encryptedTextDTO = DtoFactory.getInstance().createDto(TextDto.class).withValue(encryptedText);
        return DtoFactory.getInstance().toJson(encryptedTextDTO);
    }

    protected char[] getMasterPassword() {
        String masterPwd = System.getProperty("com.codenvy.security.masterpwd");
        if (masterPwd == null) {
            masterPwd = "changeMe";
        }
        return masterPwd.toCharArray();
    }

    public String encrypt(final String textToEncrypt) throws Exception {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getMasterPassword()));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        String encryptedText = Base64.encodeBase64String(pbeCipher.doFinal(textToEncrypt.getBytes("UTF-8")));
        return encryptedText;
    }

    public String decryptText(String textToDecrypt) throws Exception {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(getMasterPassword()));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(Base64.decodeBase64(textToDecrypt)), "UTF-8");
    }
}
