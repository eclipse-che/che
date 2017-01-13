/*
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
var USER_AGENT = navigator.userAgent.toLowerCase();
var IS_WS_SUPPORTED = ("WebSocket" in window);
if (!IS_WS_SUPPORTED || (USER_AGENT.indexOf("chrome") == -1 && USER_AGENT.indexOf("firefox") == -1 && USER_AGENT.indexOf("safari") == -1)) {
    if (document.body != null) {
        document.body.appendChild(createDivContainer());
    }

    function createDivContainer() {
        if (USER_AGENT.indexOf("msie 7") != -1 || USER_AGENT.indexOf("msie 8") != -1 || USER_AGENT.indexOf("msie 9") != -1) {
            var container = document.createElement('<div style="position:relative; height: 290px; margin: 20px auto 0; padding-top: 10px; width: 600px; text-align:center; font-family: Verdana, Bitstream Vera Sans, sans-serif; font-size:16px; border-top: 1px solid #DBDFE5; border-bottom: 1px solid #DBDFE5"></div>');
        }
        else {
            var container = document.createElement('div');
            container.setAttribute('style', 'position:relative; height: 290px; margin: 20px auto 0; padding-top: 10px; width: 600px; text-align:center; font-family: Verdana, Bitstream Vera Sans, sans-serif; font-size:16px; border-top: 1px solid #DBDFE5; border-bottom: 1px solid #DBDFE5');
        }
        container.innerHTML = '<div style="height: 50px; margin:0 auto; font-size:18px; color: #2C578A; font-weight: bold">It looks like you&#39;re using a browser that isn&#39;t supported.</div><div style="height: 50px; margin:0 auto; font-size:16px; color: #707070">To use Codenvy, upgrade to the latest one of these supported browsers.</div><div style="position: relative; height: 175px; background-color: #EFEFF5;"><div style="position: absolute; left: 65px; font-weight: bold;"><a href="http://www.google.com/chrome/" target="_blank" style="text-decoration: none; cursor: pointer;"><div style="width: 120px; height: 25px; padding-top: 130px; background: url(chrome.png) no-repeat; color: #2C578A;">Google</br>Chrome</div></a></div><div style="position: absolute; left: 234px; font-family: Verdana, Bitstream Vera Sans, sans-serif; font-weight: bold;"><a href="http://www.firefox.com" target="_blank" style="text-decoration: none; cursor: pointer;"><div style="width: 120px; height: 25px; padding-top: 130px; background: url(firefox.png) no-repeat; color: #2C578A;">Mozilla</br>Firefox</div></a></div><div style="position: absolute; left: 396px; font-weight: bold;"><a href="http://support.apple.com/downloads/#safari" target="_blank" style="text-decoration: none; cursor: pointer;"><div style="width: 120px; height: 25px; padding-top: 130px; background: url(safari.png) no-repeat; color: #2C578A;">Apple</br>Safari</div></a></div></div>';
        return container;
    }
}
