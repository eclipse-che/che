package org.eclipse.che.commons.auth.token;

import javax.servlet.http.HttpServletRequest;

/**
 * Try to extract token from request in 3 steps.
 * 1. From query parameter.
 * 2. From header.
 * 3. From cookie.
 *
 * @author Sergii Kabashniuk
 */
public class ChainedTokenExtractor implements RequestTokenExtractor {

    private final HeaderRequestTokenExtractor headerRequestTokenExtractor;

    private final QueryRequestTokenExtractor queryRequestTokenExtractor;

    public ChainedTokenExtractor() {
        headerRequestTokenExtractor = new HeaderRequestTokenExtractor();
        queryRequestTokenExtractor = new QueryRequestTokenExtractor();
    }

    @Override
    public String getToken(HttpServletRequest req) {
        String token;
        if ((token = queryRequestTokenExtractor.getToken(req)) == null) {
            token = headerRequestTokenExtractor.getToken(req);
        }
        return token;
    }
}
