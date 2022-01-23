package mnm.mods.tabbychat;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.ChannelStatus;
import mnm.mods.tabbychat.api.Message;
import mnm.mods.tabbychat.api.events.MessageAddedToChannelEvent;
import mnm.mods.tabbychat.gui.ChatArea;
import mnm.mods.tabbychat.gui.settings.GuiSettingsChannel;
import mnm.mods.tabbychat.util.ChannelPatterns;
import mnm.mods.tabbychat.util.ChatTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class ChatChannel implements Channel {

    public static final Channel DEFAULT_CHANNEL = new ChatChannel("*", false) {
        // Don't mess with this channel
        @Override
        public void setAlias(String alias) {}

        @Override
        public void setPrefix(String prefix) {}

        @Override
        public void setPrefixHidden(boolean hidden) {}

        @Override
        public void setCommand(String command) {}

        @Override
        public void openSettings() {
            // There are no settings for this channel
            TabbyChat.getInstance().openSettings(null);
        }
    };

    private transient List<Message> messages;

    private final String name;
    private final boolean isPm;
    private String alias;

    private String prefix = "";
    private boolean prefixHidden = false;

    private String command = "";

    private transient ChannelStatus status;

    ChatChannel(String name, boolean pm) {
        this.name = name;
        this.isPm = pm;
        this.alias = this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isPm() {
        return isPm;
    }

    @Override
    public String getAlias() {
        return this.alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String getPrefix() {
        return Strings.nullToEmpty(this.prefix);
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = StringUtils.stripControlCodes(prefix);
    }

    @Override
    public boolean isPrefixHidden() {
        return this.prefixHidden;
    }

    @Override
    public void setPrefixHidden(boolean hidden) {
        this.prefixHidden = hidden;
    }

    @Override
    public String getCommand() {
        return Strings.nullToEmpty(command);
    }

    @Override
    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public ChannelStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ChannelStatus status) {
        // priorities
        if (status == null || this.status == null
                || status.ordinal() < this.status.ordinal()) {
            this.status = status;
        }
        if (status == ChannelStatus.ACTIVE) {
            getManager().getChatBox().getChatArea().setChannel(this);
        }
    }

    @Override
    public void openSettings() {
        TabbyChat.getInstance().openSettings(new GuiSettingsChannel(this));
    }

    @Override
    public List<Message> getMessages() {
        if (messages == null) {
            // dumb gson
            messages = Collections.synchronizedList(Lists.<Message> newArrayList());
        }
        return messages;
    }

    @Override
    public void addMessage(ITextComponent chat) {
        addMessage(chat, 0);
    }

    @Override
    public void addMessage(ITextComponent chat, int id) {
        List<Channel> channels = TabbyChat.getInstance().getChat().getChannels();
        if (!channels.contains(this)) {
            TabbyChat.getInstance().getChat().addChannel(this);
        }
        if (id != 0) {
            removeMessages(id);
        }
        MessageAddedToChannelEvent event = new MessageAddedToChannelEvent(chat.createCopy(), id, this);
        TabbyChat.getInstance().getBus().post(event);
        if (event.text == null) {
            return;
        }
        if (TabbyChat.getInstance().settings.advanced.hideTag.get() && this != DEFAULT_CHANNEL) {
            ChannelPatterns pattern = TabbyChat.getInstance().serverSettings.general.channelPattern.get();
            Matcher matcher = pattern.getPattern().matcher(event.text.getUnformattedText());
            if (matcher.find()) {
                event.text = ChatTextUtils.subChat(event.text, matcher.end());
            }
        }

        int uc = Minecraft.getMinecraft().ingameGUI.getUpdateCounter();
        Message msg = new ChatMessage(uc, event.text, id, true);
        this.getMessages().add(0, msg);

        // compensate scrolling
        ChatArea chatbox = ((ChatManager) TabbyChat.getInstance().getChat()).getChatBox().getChatArea();
        if (getStatus() == ChannelStatus.ACTIVE && chatbox.getScrollPos() > 0 && id == 0) {
            chatbox.scroll(1);
        }

        trim(TabbyChat.getInstance().settings.advanced.historyLen.get());

        ((ChatManager) TabbyChat.getInstance().getChat()).save();
        dirty();
    }

    private void trim(int size) {
        List<Message> messages = this.getMessages();
        while (messages.size() > size) {
            messages.remove(messages.size() - 1);
        }
    }

    @Override
    public void removeMessageAt(int pos) {
        this.getMessages().remove(pos);
        ((ChatManager) TabbyChat.getInstance().getChat()).save();
        dirty();
    }

    @Override
    public void removeMessages(int id) {
        this.getMessages().removeIf(msg -> msg.getID() == id);
        ((ChatManager) TabbyChat.getInstance().getChat()).save();
        dirty();
    }

    @Override
    public void clear() {
        this.getMessages().clear();
        dirty();
    }

    private void dirty() {
        if (this.getStatus() == ChannelStatus.ACTIVE)
            getManager().getChatBox().getChatArea().markDirty();
    }

    private static ChatManager getManager() {
        return (ChatManager) TabbyChat.getInstance().getChat();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isPm ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ChatChannel))
            return false;
        ChatChannel other = (ChatChannel) obj;
        if (isPm != other.isPm)
            return false;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

    @Override
    public String toString() {
        return (isPm ? "@" : "#") + name;
    }

}
