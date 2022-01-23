package mnm.mods.tabbychat.settings;

import mnm.mods.util.config.Value;
import mnm.mods.util.config.ValueList;
import mnm.mods.util.config.ValueObject;

public class GeneralServerSettings extends ValueObject {

    public Value<Boolean> useDefaultTab = value(true);
    public ValueList<String> ignoredChannels = list();
    public Value<String> defaultChannel = value("");
    public Value<String> channelCommand = value("");
    public Value<String> messageCommand = value("");
}
