<#assign letterTitle=msg("passwordResetTitle")>
<#include "header.ftl">

${msg("passwordResetLetterText", realmName)}
${link}
${msg("emailLinkExpirationText", linkExpiration)}

<#include "footer.ftl">
