/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.api;

import io.github.axolotlclient.api.chat.ChatScreen;
import io.github.axolotlclient.api.requests.ChannelRequest;
import io.github.axolotlclient.api.requests.FriendRequest;
import io.github.axolotlclient.api.util.AlphabeticalComparator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class FriendsScreen extends Screen {

	private final Screen parent;

	private UserListWidget widget;

	private ButtonWidget chatButton, removeButton, onlineTab, allTab, pendingTab, blockedTab;
	private ButtonWidget denyButton, acceptButton, unblockButton, cancelButton;

	private Tab current = Tab.ONLINE;

	protected FriendsScreen(Screen parent, Tab tab) {
		this(parent);
		current = tab;
	}

	public FriendsScreen(Screen parent) {
		super(Text.translatable("api.screen.friends"));
		this.parent = parent;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 20, 16777215);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (keyCode == 294) {
			this.refresh();
			return true;
		} else if (this.widget.getSelectedOrNull() != null) {
			if (keyCode != 257 && keyCode != 335) {
				return this.widget.keyPressed(keyCode, scanCode, modifiers);
			} else {
				this.openChat();
				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	protected void init() {
		addDrawableChild(widget = new UserListWidget(this, client, width, height, 32, height - 64, 35));

		widget.children().clear();

		if (current == Tab.ALL || current == Tab.ONLINE) {
			FriendRequest.getInstance().getFriends().whenCompleteAsync((list, t) -> widget.setUsers(list.stream().sorted((u1, u2) ->
				new AlphabeticalComparator().compare(u1.getName(), u2.getName())).filter(user -> {
				if (current == Tab.ONLINE) {
					return user.getStatus().isOnline();
				}
				return true;
			}).toList()));
		} else if (current == Tab.PENDING) {
			FriendRequest.getInstance().getFriendRequests().whenCompleteAsync((con, th) -> {

				con.getLeft().stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
					.forEach(user -> widget.addEntry(new UserListWidget.UserListEntry(user, Text.translatable("api.friends.pending.incoming"))));
				con.getRight().stream().sorted((u1, u2) -> new AlphabeticalComparator().compare(u1.getName(), u2.getName()))
					.forEach(user -> widget.addEntry(new UserListWidget.UserListEntry(user, Text.translatable("api.friends.pending.outgoing")).outgoing()));
			});
		} else if (current == Tab.BLOCKED) {
			FriendRequest.getInstance().getBlocked().whenCompleteAsync((list, th) -> widget.setUsers(list.stream().sorted((u1, u2) ->
				new AlphabeticalComparator().compare(u1.getName(), u2.getName())).toList()));
		}

		this.addDrawableChild(blockedTab = ButtonWidget.builder(Text.translatable("api.friends.tab.blocked"), button ->
				client.setScreen(new FriendsScreen(parent, Tab.BLOCKED)))
			.positionAndSize(this.width / 2 + 24, this.height - 52, 57, 20).build());

		this.addDrawableChild(pendingTab = ButtonWidget.builder(Text.translatable("api.friends.tab.pending"), button ->
				client.setScreen(new FriendsScreen(parent, Tab.PENDING)))
			.positionAndSize(this.width / 2 - 34, this.height - 52, 57, 20).build());

		this.addDrawableChild(allTab = ButtonWidget.builder(Text.translatable("api.friends.tab.all"), button ->
				client.setScreen(new FriendsScreen(parent, Tab.ALL)))
			.positionAndSize(this.width / 2 - 94, this.height - 52, 57, 20).build());

		this.addDrawableChild(onlineTab = ButtonWidget.builder(Text.translatable("api.friends.tab.online"), button ->
				client.setScreen(new FriendsScreen(parent, Tab.ONLINE)))
			.positionAndSize(this.width / 2 - 154, this.height - 52, 57, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("api.friends.add"),
				button -> client.setScreen(new AddFriendScreen(this)))
			.positionAndSize(this.width / 2 + 88, this.height - 52, 66, 20).build());

		this.removeButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("api.friends.remove"), button -> {
			UserListWidget.UserListEntry entry = this.widget.getSelectedOrNull();
			if (entry != null) {
				removeButton.active = false;
				FriendRequest.getInstance().removeFriend(entry.getUser()).thenRun(() -> client.submit(this::refresh));
			}
		}).positionAndSize(this.width / 2 - 50, this.height - 28, 100, 20).build());

		addDrawableChild(denyButton = new ButtonWidget.Builder(Text.translatable("api.friends.request.deny"),
			button -> denyRequest()).positionAndSize(this.width / 2 - 50, this.height - 28, 48, 20).build());

		addDrawableChild(acceptButton = new ButtonWidget.Builder(Text.translatable("api.friends.request.accept"),
			button -> acceptRequest()).positionAndSize(this.width / 2 + 2, this.height - 28, 48, 20).build());

		unblockButton = addDrawableChild(ButtonWidget.builder(Text.translatable("api.users.unblock"),
			b -> {
				b.active = false;
				FriendRequest.getInstance().unblockUser(widget.getSelectedOrNull().getUser()).thenRun(() -> client.execute(this::refresh));
			}).positionAndSize(this.width / 2 - 50, this.height - 28, 100, 20).build());
		cancelButton = addDrawableChild(ButtonWidget.builder(CommonTexts.CANCEL, b -> {
			b.active = false;
			FriendRequest.getInstance().cancelFriendRequest(widget.getSelectedOrNull().getUser()).thenRun(() -> client.execute(this::refresh));
		}).positionAndSize(this.width / 2 - 50, this.height - 28, 100, 20).build());

		this.addDrawableChild(chatButton = ButtonWidget.builder(Text.translatable("api.friends.chat"), button -> openChat())
			.positionAndSize(this.width / 2 - 154, this.height - 28, 100, 20)
			.build()
		);

		this.addDrawableChild(
			ButtonWidget.builder(CommonTexts.BACK, button -> this.client.setScreen(this.parent))
				.positionAndSize(this.width / 2 + 4 + 50, this.height - 28, 100, 20)
				.build()
		);
		updateButtonActivationStates();
	}

	private void refresh() {
		client.setScreen(new FriendsScreen(parent));
	}

	private void denyRequest() {
		UserListWidget.UserListEntry entry = widget.getSelectedOrNull();
		if (entry != null) {
			denyButton.active = false;
			FriendRequest.getInstance().denyFriendRequest(entry.getUser()).thenRun(() -> client.submit(this::refresh));
		}
	}

	private void acceptRequest() {
		UserListWidget.UserListEntry entry = widget.getSelectedOrNull();
		if (entry != null) {
			acceptButton.active = false;
			FriendRequest.getInstance().acceptFriendRequest(entry.getUser()).thenRun(() -> client.submit(this::refresh));
		}
	}

	private void updateButtonActivationStates() {
		UserListWidget.UserListEntry entry = widget.getSelectedOrNull();
		chatButton.active = entry != null && (current == Tab.ALL || current == Tab.ONLINE);

		removeButton.visible = true;
		unblockButton.active = removeButton.active = entry != null;
		denyButton.visible = false;
		acceptButton.visible = unblockButton.visible = cancelButton.visible = false;
		if (current == Tab.ONLINE) {
			onlineTab.active = false;
			allTab.active = pendingTab.active = blockedTab.active = true;
		} else if (current == Tab.ALL) {
			allTab.active = false;
			onlineTab.active = pendingTab.active = blockedTab.active = true;
		} else if (current == Tab.PENDING) {
			pendingTab.active = false;
			onlineTab.active = allTab.active = blockedTab.active = true;
			removeButton.visible = false;

			if (entry != null && entry.isOutgoingRequest()) {
				cancelButton.visible = true;
			} else {
				denyButton.visible = true;
				acceptButton.visible = true;
			}
			denyButton.active = acceptButton.active = entry != null;
		} else if (current == Tab.BLOCKED) {
			blockedTab.active = false;
			onlineTab.active = allTab.active = pendingTab.active = true;
			removeButton.visible = false;
			unblockButton.visible = true;
		}
	}

	public void openChat() {
		if (!chatButton.active) {
			return;
		}
		UserListWidget.UserListEntry entry = widget.getSelectedOrNull();
		if (entry != null) {
			chatButton.active = false;
			ChannelRequest.getOrCreateDM(entry.getUser())
				.thenAccept(c -> client.execute(() -> client.setScreen(new ChatScreen(this, c))));
		}
	}

	public void select(UserListWidget.UserListEntry entry) {
		this.widget.setSelected(entry);
		this.updateButtonActivationStates();
	}

	public enum Tab {
		ONLINE,
		ALL,
		PENDING,
		BLOCKED
	}
}