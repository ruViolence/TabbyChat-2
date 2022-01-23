package mnm.mods.tabbychat;

import java.util.Calendar;
import java.util.Date;

import com.google.gson.annotations.Expose;

import mnm.mods.tabbychat.api.Message;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.text.ITextComponent;

public class ChatMessage implements Message {

    @Expose
    private ITextComponent message;
    @Expose
    private int id;
    private transient int counter;
    @Expose
    private Date date;

    public ChatMessage(int updatedCounter, ITextComponent chat, int id, boolean isNew) {
        // super(updatedCounter, chat, id);
        this.message = chat;
        this.id = id;
        this.counter = updatedCounter;
        if (isNew) {
            this.date = Calendar.getInstance().getTime();
        }
    }

    public ChatMessage(ChatLine chatline) {
        this(chatline.getUpdatedCounter(), chatline.getChatComponent(), chatline.getChatLineID(), true);
    }

    @Override
    public ITextComponent getMessage() {
        return this.message;
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

}
