<#assign letterTitle=msg("emailVerificationTitle")>
<#include "header.ftl">

${msg("emailVerificationLetterText")}
${link}
${msg("emailLinkExpirationText", linkExpiration)}

<#include "footer.ftl">
