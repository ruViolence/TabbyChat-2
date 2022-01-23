package mnm.mods.tabbychat.gui.settings;

import static mnm.mods.tabbychat.util.Translation.*;

import mnm.mods.tabbychat.TabbyChat;
import mnm.mods.tabbychat.settings.GeneralSettings;
import mnm.mods.tabbychat.settings.TabbySettings;
import mnm.mods.util.Color;
import mnm.mods.util.gui.GuiGridLayout;
import mnm.mods.util.gui.GuiLabel;
import mnm.mods.util.gui.config.GuiSettingBoolean;
import mnm.mods.util.gui.config.SettingPanel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiSettingsGeneral extends SettingPanel<TabbySettings> {

    public GuiSettingsGeneral() {
        setLayout(new GuiGridLayout(10, 20));
        setDisplayString(I18n.format(SETTINGS_GENERAL));
        setSecondaryColor(Color.of(255, 0, 255, 64));
    }

    @Override
    public void initGUI() {
        GeneralSettings sett = getSettings().general;

        int pos = 1;
        addComponent(new GuiLabel(new TextComponentTranslation(LOG_CHAT)), new int[] { 2, pos });
        GuiSettingBoolean chkLogChat = new GuiSettingBoolean(sett.logChat);
        chkLogChat.setCaption(new TextComponentTranslation(LOG_CHAT_DESC));
        addComponent(chkLogChat, new int[] { 1, pos });

        addComponent(new GuiLabel(new TextComponentTranslation(SPLIT_LOG)), new int[] { 7, pos });
        GuiSettingBoolean chkSplitLog = new GuiSettingBoolean(sett.splitLog);
        chkSplitLog.setCaption(new TextComponentTranslation(SPLIT_LOG_DESC));
        addComponent(chkSplitLog, new int[] { 6, pos });

        pos += 2;
        addComponent(new GuiLabel(new TextComponentTranslation(UNREAD_FLASHING)), new int[] { 2, pos });
        addComponent(new GuiSettingBoolean(sett.unreadFlashing), new int[] { 1, pos });
    }

    @Override
    public TabbySettings getSettings() {
        return TabbyChat.getInstance().settings;
    }

}
