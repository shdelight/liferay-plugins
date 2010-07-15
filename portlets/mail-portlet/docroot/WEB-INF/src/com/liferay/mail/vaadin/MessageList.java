/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.mail.vaadin;

import com.liferay.mail.mailbox.Mailbox;
import com.liferay.mail.mailbox.MailboxFactoryUtil;
import com.liferay.mail.model.Folder;
import com.liferay.mail.model.Message;
import com.liferay.mail.service.MessageLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

/**
 * @author Henri Sara
 */
public class MessageList extends CustomComponent {

	private static Log _log = LogFactoryUtil.getLog(MessageList.class);

	public static final String STYLE_IMPORTANT = "message-important";
	public static final String STYLE_NOT_SEEN = "message-not-seen";

	private VerticalLayout mainLayout;
	private MessageListTable table;
	private MessageToolbar top;

	private MessageView messageView;
	private MainMailView mainMailView;
	private Long accountId;
	private Folder folder;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization. The constructor will not be
	 * automatically regenerated by the visual editor.
	 *
	 * @param mainPopupWindow
	 */
	public MessageList(MainMailView mainMailView, MessageView messageView) {

		this.mainMailView = mainMailView;
		this.messageView = messageView;
		buildMainLayout();
		setCompositionRoot(mainLayout);

		mainLayout.setSizeFull();
		mainLayout.setExpandRatio(table, 1.0f);

	}

	protected void showMessage(Message msg) {

		if (Validator.isNotNull(msg) && Validator.isNull(msg.getBody())) {
			try {
				msg = MessageUtil.getFullMessage(msg, false);
			} catch (PortalException e) {
				_log.debug(e, e);
				// TODO might not be the best message?
				Controller.get().showError(Lang.get("unable-to-connect-with-mail-server"), e);
			} catch (SystemException e) {
				// TODO might not be the best message?
				_log.debug(e, e);
				Controller.get().showError(Lang.get("unable-to-connect-with-mail-server"), e);
			}
			// if there were errors, continue and display message with empty body etc.
		}

		messageView.showMessage(msg);
		// Mark message as seen
		if (msg != null && !MessageUtil.isSeen(msg)) {
			try {
				MessageUtil.markMessageRead(msg, true);
			} catch (PortalException e) {
				Controller.get().showError(Lang.get("unable-to-flag-messages"),
						e);
			} catch (SystemException e) {
				Controller.get().showError(Lang.get("unable-to-flag-messages"),
						e);
			}

		}
	}

	private VerticalLayout buildMainLayout() {

		// common part: create layout
		mainLayout = new VerticalLayout();

		// top-level component properties
		setHeight("100.0%");
		setWidth("100.0%");

		// top
		top = new MessageToolbar(mainMailView);
		top.setImmediate(false);
		top.setHeight(null);
		top.setWidth("100.0%");
		mainLayout.addComponent(top);

		// table
		table = new MessageListTable(this);
		mainLayout.addComponent(table);

		return mainLayout;
	}

	/**
	 * Returns the currently selected messages. If one more more message is
	 * checked, these are returned. If no messages are checked, the selected
	 * message is returned.
	 *
	 * @return
	 */
	public List<Message> getSelectedMessages() {

		return table.getSelectedMessages();
	}

	public Message getSelectedMessage() {

		return table.getSelectedMessage();
	}

	public void showMessages(Long accountId, Folder folder) {

		this.accountId = accountId;
		this.folder = folder;

		table.showMessages(accountId, folder);
		top.updateToolbar(accountId, folder);

	}

	public MessageListTable getTable(){
		return table;
	}

	public void refresh() {
		showMessages(accountId, folder);
	}

}