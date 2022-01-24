package mnm.mods.tabbychat.api;

import com.google.common.eventbus.EventBus;

/**
 * Represents the main object for TabbyChat
 */
public abstract class TabbyAPI {

    private static TabbyAPI instance;

    protected TabbyAPI() {
        instance = this;
    }

    /**
     * Gets the API.
     *
     * @return The api
     */
    public static TabbyAPI getAPI() {
        return instance;
    }

    /**
     * Gets the chat.
     *
     * @return The chat
     */
    public abstract Chat getChat();

    /**
     * Gets the common event bus used for chat messages. For gui related things,
     * use the Component's.
     *
     * @return The event bus
     */
    public abstract EventBus getBus();

}
