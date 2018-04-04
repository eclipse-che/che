<#assign letterTitle=msg("executeActionsTitle")>
<#include "header.ftl">

${msg("executeActionsLetterText")}
${link}
${msg("emailLinkExpirationText", linkExpiration)}

<#include "footer.ftl">
