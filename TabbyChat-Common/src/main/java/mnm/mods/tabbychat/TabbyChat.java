package mnm.mods.tabbychat;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.annotation.Nullable;

import mnm.mods.tabbychat.api.AddonManager;
import mnm.mods.tabbychat.api.Chat;
import mnm.mods.tabbychat.api.TabbyAPI;
import mnm.mods.tabbychat.core.GuiChatTC;
import mnm.mods.tabbychat.core.GuiNewChatTC;
import mnm.mods.tabbychat.core.GuiSleepTC;
import mnm.mods.tabbychat.core.api.TabbyAddonManager;
import mnm.mods.tabbychat.core.api.TabbyEvents;
import mnm.mods.tabbychat.extra.ChatAddonAntiSpam;
import mnm.mods.tabbychat.extra.ChatLogging;
import mnm.mods.tabbychat.extra.filters.FilterAddon;
import mnm.mods.tabbychat.gui.settings.GuiSettingsScreen;
import mnm.mods.tabbychat.settings.AdvancedSettings;
import mnm.mods.tabbychat.settings.ChannelSettings;
import mnm.mods.tabbychat.settings.ColorSettings;
import mnm.mods.tabbychat.settings.GeneralSettings;
import mnm.mods.tabbychat.settings.ServerSettings;
import mnm.mods.tabbychat.util.TabbyRef;
import mnm.mods.util.LogHelper;
import mnm.mods.util.MnmUtils;
import mnm.mods.util.ReflectionHelper;
import mnm.mods.util.gui.SettingPanel;
import mnm.mods.util.update.UpdateChecker;
import mnm.mods.util.update.UpdateRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;

public abstract class TabbyChat extends TabbyAPI {

    private static final LogHelper LOGGER = LogHelper.getLogger(TabbyRef.MOD_ID);
    private static TabbyChat instance;

    private AddonManager addonManager;
    private TabbyEvents events;

    public GeneralSettings generalSettings;
    public AdvancedSettings advancedSettings;
    public ColorSettings colorSettings;
    // Server settings
    @Nullable
    public ChannelSettings channelSettings;
    @Nullable
    public ServerSettings serverSettings;

    private File dataFolder;
    private InetSocketAddress currentServer;

    private boolean updateChecked;

    protected static void setInstance(TabbyChat inst) {
        instance = inst;
        setAPI(inst);
    }

    public static TabbyChat getInstance() {
        return instance;
    }

    public static LogHelper getLogger() {
        return LOGGER;
    }

    @Override
    public Chat getChat() {
        return GuiNewChatTC.getInstance().getChatbox();
    }

    @Override
    public AddonManager getAddonManager() {
        return this.addonManager;
    }

    public TabbyEvents getEventManager() {
        return this.events;
    }

    public void openSettings(SettingPanel<?> setting) {
        GuiSettingsScreen screen = new GuiSettingsScreen();
        Minecraft.getMinecraft().displayGuiScreen(screen);
        if (setting != null) {
            screen.selectSetting(setting);
        }
    }

    public InetSocketAddress getCurrentServer() {
        return this.currentServer;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void registerSettings(Class<? extends SettingPanel<?>> setting) {
        GuiSettingsScreen.registerSetting(setting);
    }

    // Protected methods

    protected abstract void loadResourcePack(File source, String name);

    protected void setConfigFolder(File config) {
        this.dataFolder = new File(config, TabbyRef.MOD_ID);
    }

    protected void init() {
        loadUtils();

        addonManager = new TabbyAddonManager();
        events = new TabbyEvents(addonManager);

        // Set global settings
        generalSettings = new GeneralSettings();
        advancedSettings = new AdvancedSettings();
        colorSettings = new ColorSettings();

        // Load settings
        generalSettings.loadSettingsFile();
        advancedSettings.loadSettingsFile();
        colorSettings.loadSettingsFile();

        // Save settings
        generalSettings.saveSettingsFile();
        advancedSettings.saveSettingsFile();
        colorSettings.saveSettingsFile();

        addonManager.registerListener(new ChatAddonAntiSpam());
        addonManager.registerListener(new FilterAddon());
        addonManager.registerListener(new ChatLogging(new File("logs/chat")));

        MnmUtils.getInstance().setChatProxy(new TabbedChatProxy());
    }

    protected void onRender(GuiScreen currentScreen) {
        if (currentScreen instanceof GuiChat && !(currentScreen instanceof GuiChatTC)) {
            Minecraft mc = Minecraft.getMinecraft();
            // Get the default text via Reflection
            String inputBuffer = "";
            try {
                inputBuffer = (String) ReflectionHelper.getFieldValue(GuiChat.class, currentScreen,
                        TabbyRef.DEFAULT_INPUT_FIELD_TEXT);
            } catch (Exception e) {}
            if (currentScreen instanceof GuiSleepMP) {
                mc.displayGuiScreen(new GuiSleepTC());
            } else {
                mc.displayGuiScreen(new GuiChatTC(inputBuffer));
            }
        }
    }

    protected void onJoin(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            this.currentServer = (InetSocketAddress) address;
        }

        // Set server settings
        channelSettings = new ChannelSettings(currentServer);
        serverSettings = new ServerSettings(currentServer);
        channelSettings.loadSettingsFile();
        serverSettings.loadSettingsFile();
        channelSettings.saveSettingsFile();
        serverSettings.saveSettingsFile();

        try {
            hookIntoChat(Minecraft.getMinecraft().ingameGUI);
        } catch (Exception e) {
            LOGGER.fatal("Unable to hook into chat.  This is bad.", e);
        }

        // update check
        updateCheck();
    }

    private void updateCheck() {
        if (generalSettings.checkUpdates.getValue() && !updateChecked) {
            UpdateRequest request = new UpdateRequest(TabbyRef.MOD_ID);
            UpdateChecker checker = new UpdateChecker(request, TabbyRef.MOD_REVISION);
            checker.start();
            updateChecked = true;
        }
    }

    private void loadUtils() {
        File source = findClasspathRoot(MnmUtils.class);
        loadResourcePack(source, "Mnm Utils");
        try {
            Minecraft.class.getMethod("getMinecraft");
            // I'm in dev, fix things.
            loadResourcePack(findClasspathRoot(TabbyChat.class), "TabbyChat-Common");
        } catch (Exception e) {
            // unimportant
        }
    }

    private File findClasspathRoot(Class<?> clas) {
        String str = clas.getProtectionDomain().getCodeSource().getLocation().toString();
        str = str.replace("/" + clas.getCanonicalName().replace('.', '/').concat(".class"), "");
        str = str.replace('\\', '/');
        if (str.endsWith("!")) {
            str = str.substring(0, str.length() - 1);
        }
        if (str.startsWith("jar:")) {
            str = str.substring(4);
        }
        if (str.startsWith("file:/")) {
            str = str.substring(6);
        }
        return new File(str);
    }

    private void hookIntoChat(GuiIngame guiIngame) throws Exception {
        if (!GuiNewChatTC.class.isAssignableFrom(guiIngame.getChatGUI().getClass())) {
            // guiIngame.persistantChatGUI = GuiNewChatTC.getInstance();
            ReflectionHelper.setFieldValue(GuiIngame.class, guiIngame, GuiNewChatTC.getInstance(),
                    TabbyRef.PERSISTANT_CHAT_GUI);
            LOGGER.info("Successfully hooked into chat.");
        }
    }
}