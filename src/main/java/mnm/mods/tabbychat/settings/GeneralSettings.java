package mnm.mods.tabbychat.settings;

import mnm.mods.util.config.Value;
import mnm.mods.util.config.ValueObject;

public class GeneralSettings extends ValueObject {

    public Value<Boolean> logChat = value(true);
    public Value<Boolean> splitLog = value(true);
    public Value<Boolean> unreadFlashing = value(true);
}
