package mnm.mods.tabbychat;

import com.google.common.eventbus.EventBus;
import com.mumfrey.liteloader.core.LiteLoader;
import mnm.mods.tabbychat.api.ChannelStatus;
import mnm.mods.tabbychat.api.TabbyAPI;
import mnm.mods.tabbychat.core.GuiNewChatTC;
import mnm.mods.tabbychat.core.mixin.IGuiIngame;
import mnm.mods.tabbychat.extra.ChatLogging;
import mnm.mods.tabbychat.extra.filters.FilterAddon;
import mnm.mods.tabbychat.gui.settings.GuiSettingsScreen;
import mnm.mods.tabbychat.settings.ServerSettings;
import mnm.mods.tabbychat.settings.TabbySettings;
import mnm.mods.tabbychat.util.TabbyRef;
import mnm.mods.util.MnmUtils;
import mnm.mods.util.gui.config.SettingPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.annotation.Nullable;

public class TabbyChat extends TabbyAPI {

    private static final Logger LOGGER = LogManager.getLogger(TabbyRef.MOD_ID);

    private ChatManager chatManager;
    private GuiNewChatTC chatGui;
    private EventBus bus = new EventBus();

    public TabbySettings settings;
    public ServerSettings serverSettings;

    private File dataFolder;
    private InetSocketAddress currentServer;

    public TabbyChat(File configPath) {
        super();
        this.dataFolder = new File(configPath, TabbyRef.MOD_ID);
    }

    public static TabbyChat getInstance() {
        return (TabbyChat) getAPI();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    @Override
    public ChatManager getChat() {
        return chatManager;
    }

    public GuiNewChatTC getChatGui() {
        return chatGui;
    }

    @Override
    public EventBus getBus() {
        return bus;
    }

    void openSettings(SettingPanel<?> setting) {
        GuiSettingsScreen screen = new GuiSettingsScreen(setting);
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    public InetSocketAddress getCurrentServer() {
        return this.currentServer;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public void init() {

        // Set global settings
        settings = new TabbySettings();
        LiteLoader.getInstance().registerExposable(settings, null);

        bus.register(new FilterAddon());
        bus.register(new ChatLogging(new File("logs/chat")));

    }

    public void postInit(MnmUtils utils) {
        // gui related stuff should be done here
        chatManager = new ChatManager(this);
        // this is set here because status relies on `chatManager`.
        ChatChannel.DEFAULT_CHANNEL.setStatus(ChannelStatus.ACTIVE);
        chatGui = new GuiNewChatTC(Minecraft.getMinecraft(), chatManager);

        utils.setChatProxy(new TabbedChatProxy());
    }

    public void onJoin(@Nullable SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            this.currentServer = (InetSocketAddress) address;
        } else {
            this.currentServer = null;
        }

        // Set server settings
        serverSettings = new ServerSettings(currentServer);
        LiteLoader.getInstance().registerExposable(serverSettings, null);

        try {
            hookIntoChat(Minecraft.getMinecraft().ingameGUI);
        } catch (Exception e) {
            LOGGER.fatal("Unable to hook into chat.  This is bad.", e);
        }
    }

    @SuppressWarnings("MixinClassReference")
    private void hookIntoChat(GuiIngame guiIngame) throws Exception {
        if (!GuiNewChatTC.class.isAssignableFrom(guiIngame.getChatGUI().getClass())) {
            ((IGuiIngame) guiIngame).setPersistantChatGUI(chatGui);
            LOGGER.info("Successfully hooked into chat.");
        }
    }
}
