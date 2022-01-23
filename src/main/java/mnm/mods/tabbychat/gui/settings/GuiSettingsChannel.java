package mnm.mods.tabbychat.gui.settings;

import static mnm.mods.tabbychat.util.Translation.*;

import com.google.common.eventbus.Subscribe;
import mnm.mods.tabbychat.TabbyChat;
import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.TabbyAPI;
import mnm.mods.tabbychat.settings.ServerSettings;
import mnm.mods.util.Color;
import mnm.mods.util.Location;
import mnm.mods.util.gui.BorderLayout;
import mnm.mods.util.gui.GuiButton;
import mnm.mods.util.gui.GuiCheckbox;
import mnm.mods.util.gui.GuiComponent;
import mnm.mods.util.gui.GuiGridLayout;
import mnm.mods.util.gui.GuiLabel;
import mnm.mods.util.gui.GuiPanel;
import mnm.mods.util.gui.GuiScrollingPanel;
import mnm.mods.util.gui.GuiText;
import mnm.mods.util.gui.VerticalLayout;
import mnm.mods.util.gui.config.SettingPanel;
import mnm.mods.util.gui.events.ActionPerformedEvent;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiSettingsChannel extends SettingPanel<ServerSettings> {

    private Channel channel;

    private GuiScrollingPanel channels;
    private GuiPanel panel;

    private GuiText alias;
    private GuiText prefix;
    private GuiCheckbox hidePrefix;
    private GuiText command;

    GuiSettingsChannel() {
        this(null);
    }

    public GuiSettingsChannel(Channel channel) {
        this.channel = channel;
        this.setLayout(new BorderLayout());
        this.setDisplayString(I18n.format(CHANNEL_TITLE));
        this.setSecondaryColor(Color.of(0, 15, 100, 65));

    }

    @Override
    public void initGUI() {
        channels = new GuiScrollingPanel();
        channels.setLocation(new Location(0, 0, 60, 200));
        channels.getContentPanel().setLayout(new VerticalLayout());
        for (Channel channel : getSettings().channels.get().values()) {
            channels.getContentPanel().addComponent(new ChannelButton(channel));
        }
        this.addComponent(channels, BorderLayout.Position.WEST);
        panel = new GuiPanel();
        panel.setLayout(new GuiGridLayout(8, 20));
        this.addComponent(panel, BorderLayout.Position.CENTER);

        this.select(channel);
    }

    private void select(Channel channel) {

        for (GuiComponent comp : channels.getContentPanel()) {
            comp.setEnabled(((ChannelButton) comp).channel != channel);
        }

        int pos = 1;

        this.channel = channel;
        this.panel.clearComponents();
        if (channel == null) {
            if (channels.getContentPanel().getComponentCount() > 0) {
                this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_SELECT)), new int[] { 1, pos });
            } else {
                this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_NONE)), new int[] { 1, pos });
            }
            return;
        }
        this.panel.addComponent(
                new GuiLabel(new TextComponentTranslation(CHANNEL_LABEL, channel.getName())),
                new int[] { 1, pos });

        pos += 3;
        this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_ALIAS)), new int[] { 1, pos });
        this.panel.addComponent(alias = new GuiText(), new int[] { 3, pos, 4, 1 });
        alias.setValue(channel.getAlias());

        pos += 2;
        this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_PREFIX)), new int[] { 1, pos });
        this.panel.addComponent(prefix = new GuiText(), new int[] { 3, pos, 4, 1 });
        prefix.setValue(channel.getPrefix());

        pos += 2;
        this.panel.addComponent(hidePrefix = new GuiCheckbox(), new int[] { 1, pos });
        hidePrefix.setValue(channel.isPrefixHidden());
        this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_HIDE_PREFIX)), new int[] { 2, pos });

        pos += 2;
        this.panel.addComponent(command = new GuiText(), new int[] { 3, pos, 4, 1 });
        command.setValue(channel.getCommand());
        this.panel.addComponent(new GuiLabel(new TextComponentTranslation(CHANNEL_COMMAND)), new int[] { 1, pos });

        GuiButton accept = new GuiButton(I18n.format("gui.done"));
        accept.getBus().register(new Object() {
            @Subscribe
            public void somebodySaveMe(ActionPerformedEvent event) {
                save();
            }
        });
        this.panel.addComponent(accept, new int[] { 2, 15, 4, 2 });

        GuiButton forget = new GuiButton(I18n.format(CHANNEL_FORGET));
        forget.getBus().register(new Object() {
            @Subscribe
            public void oohShinyObject(ActionPerformedEvent event) {
                Channel channel = GuiSettingsChannel.this.channel;
                // remove from chat
                TabbyAPI.getAPI().getChat().removeChannel(channel);
                // remove from settings file
                getSettings().channels.get().remove(channel.getName());
                if (!channel.isPm()) {
                    // don't add this channel again.
                    getSettings().general.ignoredChannels.add(channel.getName());
                }
                // remove from settings gui
                for (GuiComponent comp : channels.getContentPanel()) {
                    if (comp instanceof ChannelButton && ((ChannelButton) comp).channel == channel) {
                        channels.getContentPanel().removeComponent(comp);
                        break;
                    }
                }
                select(null);
            }
        });
        this.panel.addComponent(forget, new int[] { 2, 17, 4, 2 });
    }

    private void save() {
        channel.setAlias(alias.getValue());
        channel.setPrefix(prefix.getValue());
        channel.setPrefixHidden(hidePrefix.getValue());
        channel.setCommand(command.getValue());
    }

    @Override
    public ServerSettings getSettings() {
        return TabbyChat.getInstance().serverSettings;
    }

    public class ChannelButton extends GuiButton {

        private Channel channel;

        ChannelButton(Channel channel) {
            super(channel.getName());
            this.channel = channel;
            setLocation(new Location(0, 0, 60, 15));
        }

        @Subscribe
        public void margeChangeTheChannel(ActionPerformedEvent event) {
            select(channel);
        }
    }
}
