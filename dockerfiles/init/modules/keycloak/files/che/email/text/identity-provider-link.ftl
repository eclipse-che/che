<#assign letterTitle=msg("identityProviderLinkTitle")>
<#include "header.ftl">

${msg("identityProviderLinkLetterText", identityProviderAlias, realmName, identityProviderContext.username)}
${link}
${msg("emailLinkExpirationText", linkExpiration)}

<#include "footer.ftl">
