/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

@SuppressWarnings("serial")
public class PopupMenuSiteNote extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = -5692544221103745600L;

    private static final Logger LOGGER = LogManager.getLogger(PopupMenuSiteNote.class);

    private final ExtensionHistory extension;

    public PopupMenuSiteNote(ExtensionHistory extension) {
        super(Constant.messages.getString("history.note.popup"));

        this.extension = extension;
    }

    @Override
    protected boolean isEnableForInvoker(
            Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return invoker == Invoker.SITES_PANEL;
    }

    @Override
    public void performAction(SiteNode siteNode) {
        try {
            extension.showSiteNotesAddDialog(siteNode);

        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
