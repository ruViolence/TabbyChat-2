package mnm.mods.tabbychat.api;

import java.util.Date;
import java.util.List;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * Represents a message.
 */
public interface Message {

    /**
     * Gets the message
     *
     * @return The message
     */
    ITextComponent getMessage();
    
    List<ITextComponent> getMessageSplit(int width);

    /**
     * Gets the update counter used for this message.
     *
     * @return The counter
     */
    int getCounter();

    /**
     * Gets the ID of this message. 0 is a normal message.
     *
     * @return The ID of this message
     */
    int getID();

    /**
     * Gets the date that this message was sent.
     *
     * @return The date
     */
    Date getDate();

    @Nullable
    Message getParent(); 


}
