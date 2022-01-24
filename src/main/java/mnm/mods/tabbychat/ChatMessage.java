package mnm.mods.tabbychat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;

import mnm.mods.tabbychat.api.Message;
import mnm.mods.tabbychat.util.ChatTextUtils;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public class ChatMessage implements Message {

    @Expose
    private ITextComponent message;
    private transient int lastWidth;
    private transient List<ITextComponent> messageSplit;
    @Expose
    private int id;
    private transient int counter;
    @Expose
    private Date date;
    private transient Message parent;

    public ChatMessage(int updatedCounter, ITextComponent chat, int id, boolean isNew) {
        this(updatedCounter, chat, id, isNew, null);
    }

    public ChatMessage(int updatedCounter, ITextComponent chat, int id, boolean isNew, Message parent) {
        // super(updatedCounter, chat, id);
        this.message = chat;
        this.id = id;
        this.counter = updatedCounter;
        if (isNew) {
            this.date = Calendar.getInstance().getTime();
        }
        this.parent = parent;
    }

    public ChatMessage(ChatLine chatline) {
        this(chatline.getUpdatedCounter(), chatline.getChatComponent(), chatline.getChatLineID(), true);
    }

    @Override
    public ITextComponent getMessage() {
        return this.message;
    }

    @Override
    public List<ITextComponent> getMessageSplit(int width) {
        List<ITextComponent> cachedSplit = this.messageSplit;

        if (cachedSplit == null || lastWidth != width) {
            List<ITextComponent> split = ChatTextUtils.split(getMessage(), width);
            this.messageSplit = split;
            this.lastWidth = width;
            return split;
        }

        return cachedSplit;
    }

    @Override
    public int getCounter() {
        return this.counter;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Nullable
    @Override
    public Message getParent() {
        return this.parent;
    }

}
