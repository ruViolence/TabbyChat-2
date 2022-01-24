package mnm.mods.tabbychat.core;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import mnm.mods.tabbychat.ChatChannel;
import mnm.mods.tabbychat.ChatManager;
import mnm.mods.tabbychat.TabbyChat;
import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.ChannelStatus;
import mnm.mods.tabbychat.api.events.ChatMessageEvent.ChatReceivedEvent;
import mnm.mods.tabbychat.api.gui.ChatScreen;
import mnm.mods.tabbychat.gui.ChatBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;

import java.util.Set;
import java.util.stream.Collectors;

public class GuiNewChatTC extends GuiNewChat implements ChatScreen {

    private static GuiNewChatTC instance;

    private Minecraft mc;

    private TabbyChat tc = TabbyChat.getInstance();
    private ChatManager chat;
    private EventBus bus;

    private int prevScreenWidth;
    private int prevScreenHeight;

    public GuiNewChatTC(Minecraft minecraft, ChatManager manager) {
        super(minecraft);
        mc = minecraft;
        chat = manager;
        bus = new EventBus();
        instance = this;

        this.prevScreenHeight = mc.displayHeight;
    }

    public static GuiNewChat getInstance() {
        return instance;
    }

    @Override
    public void refreshChat() {
        chat.getChatBox().updateComponent();
    }

    @Override
    public void clearChatMessages(boolean sent) {
        chat.clearMessages();
        if (sent) {
            this.getSentMessages().clear();
        }
    }

    @Override
    public void drawChat(int i) {
        if (prevScreenHeight != mc.displayHeight || prevScreenWidth != mc.displayWidth) {

            chat.getChatBox().onScreenHeightResize(prevScreenWidth, prevScreenHeight, mc.displayWidth, mc.displayHeight);

            prevScreenWidth = mc.displayWidth;
            prevScreenHeight = mc.displayHeight;
        }

//        if (getChatOpen())
//            return;

        ChatBox chatbox = chat.getChatBox();
        float scale = chatbox.getScale();

        GlStateManager.popMatrix(); // ignore what GuiIngame did.
        GlStateManager.pushMatrix();

        // Scale it accordingly
        GlStateManager.scale(scale, scale, 1.0F);

        // Make the upper left corner of the panel (0,0).
        GlStateManager.translate(chatbox.getBounds().x, chatbox.getBounds().y, 0.0F);

        int mouseX = Mouse.getEventX();
        int mouseY = -Mouse.getEventY() - 1;
        chatbox.drawComponent(mouseX, mouseY);

        GlStateManager.popMatrix();
        GlStateManager.pushMatrix(); // push to avoid gl errors
    }

    @Override
    public void printChatMessageWithOptionalDeletion(ITextComponent ichat, int id) {
        this.addMessage(ichat, id);
    }

    public void addMessage(ITextComponent ichat, int id) {
        // chat listeners
        ChatReceivedEvent chatevent = new ChatReceivedEvent(ichat, id);
        chatevent.channels.add(ChatChannel.DEFAULT_CHANNEL);
        tc.getBus().post(chatevent);
        // chat filters
        ichat = chatevent.text;
        id = chatevent.id;
        if (ichat != null) {
            if (id != 0) {
                // send removable msg to current channel
                chatevent.channels.clear();
                chatevent.channels.add(this.chat.getActiveChannel());
            }
            if (chatevent.channels.contains(ChatChannel.DEFAULT_CHANNEL) && chatevent.channels.size() > 1
                    && !tc.serverSettings.general.useDefaultTab.get()) {
                chatevent.channels.remove(ChatChannel.DEFAULT_CHANNEL);
            }
            boolean msg = !chatevent.channels.contains(this.chat.getActiveChannel());
            final Set<String> ignored = Sets.newHashSet(this.tc.serverSettings.general.ignoredChannels.get());
            Set<Channel> channels = chatevent.channels.stream()
                    .filter(it -> !ignored.contains(it.getName()))
                    .collect(Collectors.toSet());
            for (Channel channel : channels) {
                channel.addMessage(ichat, id);
                if (msg) {
                    channel.setStatus(ChannelStatus.UNREAD);
                }
            }
            TabbyChat.getLogger().info("[CHAT] " + ichat.getUnformattedText());
        }
    }

    @Override
    public void deleteChatLine(int id) {
        chat.removeMessages(id);
    }

    @Override
    public void resetScroll() {
        chat.getChatBox().getChatArea().resetScroll();
        super.resetScroll();
    }

    public ChatManager getChatManager() {
        return chat;
    }

    @Override
    public ITextComponent getChatComponent(int clickX, int clickY) {
        return chat.getChatBox().getChatArea().getChatComponent(clickX, clickY);
    }

    @Override
    public int getChatHeight() {
        return chat.getChatBox().getChatArea().getBounds().height;
    }

    @Override
    public int getChatWidth() {
        return chat.getChatBox().getChatArea().getBounds().width;
    }

    @Override
    public EventBus getBus() {
        return bus;
    }

}
