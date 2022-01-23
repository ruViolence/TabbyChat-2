package mnm.mods.tabbychat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.ChannelStatus;
import mnm.mods.tabbychat.api.Chat;
import mnm.mods.tabbychat.api.Message;
import mnm.mods.tabbychat.gui.ChatBox;
import mnm.mods.tabbychat.gui.TextBox;
import mnm.mods.tabbychat.settings.AdvancedSettings;
import mnm.mods.tabbychat.settings.GeneralServerSettings;
import mnm.mods.util.Location;
import mnm.mods.util.config.ValueMap;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public class ChatManager implements Chat {

    public static final int MAX_CHAT_LENGTH = 256;

    private ChatBox chatbox;

    private Map<String, Channel> allChannels = Maps.newHashMap();
    private Map<String, Channel> allPms = Maps.newHashMap();
    private List<Channel> channels = Lists.newLinkedList();
    private Channel active = ChatChannel.DEFAULT_CHANNEL;

    private Map<Channel, List<Message>> messages = Maps.newHashMap();

    public ChatManager(TabbyChat tc) {
        AdvancedSettings settings = tc.settings.advanced;
        int x = settings.chatX.get();
        int y = settings.chatY.get();
        int width = settings.chatW.get();
        int height = settings.chatH.get();

        this.chatbox = new ChatBox(new Location(x, y, width, height));

        if (!this.channels.contains(ChatChannel.DEFAULT_CHANNEL)) {
            this.channels.add(ChatChannel.DEFAULT_CHANNEL);
            chatbox.getTray().addChannel(ChatChannel.DEFAULT_CHANNEL);
        }
    }

    @Override
    public Channel getChannel(String name) {
        return getChannel(name, false);
    }

    @Override
    public Channel getChannel(String name, boolean pm) {
        return pm ? getPmChannel(name) : getChatChannel(name);
    }

    private Channel getChatChannel(String name) {
        return getChannel(name, false, this.allChannels, TabbyChat.getInstance().serverSettings.channels);
    }

    private Channel getPmChannel(String name) {
        Channel channel = getChannel(name, true, this.allPms, TabbyChat.getInstance().serverSettings.pms);
        if (channel.getPrefix().isEmpty()) {
            channel.setPrefix("/msg " + name);
        }
        return channel;
    }

    private Channel getChannel(String name, boolean pm, Map<String, Channel> from, ValueMap<ChatChannel> setting) {
        if (!from.containsKey(name)) {
            // fetch from settings
            ChatChannel chan = setting.get(name);
            if (chan == null || chan.getName() == null) {
                chan = new ChatChannel(name, pm);
                setting.get().put(chan.getName(), chan);
            }
            from.put(name, chan);
            messages.put(chan, chan.getMessages());
        }
        return from.get(name);
    }

    @Override
    public void addChannel(Channel channel) {
        if (!this.channels.contains(channel)) {
            this.channels.add(channel);
            chatbox.getTray().addChannel(channel);
        }
    }

    @Override
    public void removeChannel(Channel channel) {
        if (channels.contains(channel) && !channel.equals(ChatChannel.DEFAULT_CHANNEL)) {
            channels.remove(channel);
            chatbox.getTray().removeChannel(channel);
        }
        if (getActiveChannel() == channel) {
            setActiveChannel(ChatChannel.DEFAULT_CHANNEL);
        }
    }

    @Override
    public List<Channel> getChannels() {
        return ImmutableList.copyOf(channels);
    }

    @Override
    public void removeMessages(int id) {
        for (Channel channel : this.channels) {
            channel.removeMessages(id);
        }
    }

    @Override
    public void clearMessages() {
        for (Channel channel : channels) {
            channel.clear();
        }

        this.channels.clear();
        this.channels.add(ChatChannel.DEFAULT_CHANNEL);

        chatbox.getTray().clear();
    }

    @Override
    public Channel getActiveChannel() {
        return active;
    }

    @Override
    public void setActiveChannel(Channel channel) {
        TextBox text = chatbox.getChatInput();


        if (active.isPrefixHidden()
                ? text.getText().trim().isEmpty()
                : text.getText().trim().equals(active.getPrefix())) {
            // text is the prefix, so remove it.
            text.setText("");
            if (!channel.isPrefixHidden() && !channel.getPrefix().isEmpty()) {
                // target has prefix visible
                text.getTextField().getTextField().setText(channel.getPrefix() + " ");
            }
        }
        // set max text length
        boolean hidden = channel.isPrefixHidden();
        int prefLength = hidden ? channel.getPrefix().length() + 1 : 0;

        text.getTextField().getTextField().setMaxStringLength(MAX_CHAT_LENGTH - prefLength);

        // reset scroll
        // TODO per-channel scroll settings?
        if (channel != active) {
            chatbox.getChatArea().resetScroll();
        }
        active.setStatus(null);
        active = channel;
        active.setStatus(ChannelStatus.ACTIVE);

        runActivationCommand(channel);

    }

    private void runActivationCommand(Channel channel) {
        String cmd = channel.getCommand();
        if (cmd.isEmpty()) {

            GeneralServerSettings settings = TabbyChat.getInstance().serverSettings.general;
            String pat = channel.isPm() ? settings.messageCommand.get() : settings.channelCommand.get();

            if (pat.isEmpty()) {
                return;
            }
            String name = channel.getName();
            if (channel == ChatChannel.DEFAULT_CHANNEL) {
                name = TabbyChat.getInstance().serverSettings.general.defaultChannel.get();
            }
            // insert the channel name
            cmd = pat.replace("{}", name);

        }
        if (cmd.startsWith("/")) {
            if (cmd.length() > MAX_CHAT_LENGTH) {
                cmd = cmd.substring(0, MAX_CHAT_LENGTH);
            }
            Minecraft.getMinecraft().player.sendChatMessage(cmd);
        }
    }

    public ChatBox getChatBox() {
        return this.chatbox;
    }
}
