package icu.dannyism.twister;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

public class MidiFighterTwisterExtensionDefinition extends ControllerExtensionDefinition
{
    private static final UUID DRIVER_ID = UUID.fromString("4cf18f07-e6b4-45b7-8cf3-ebc695e5a0fd");

    public MidiFighterTwisterExtensionDefinition()
    {
    }

    @Override
    public String getName()
    {
        return "Twister4Bitwig";
    }

    @Override
    public String getAuthor()
    {
        return "Daniel Mulford";
    }

    @Override
    public String getVersion()
    {
        return "1.0.1";
    }

    @Override
    public UUID getId()
    {
        return DRIVER_ID;
    }

    @Override
    public String getHardwareVendor()
    {
        return "DJ TechTools";
    }

    @Override
    public String getHardwareModel()
    {
        return "Midi Fighter Twister";
    }

    @Override
    public int getRequiredAPIVersion()
    {
        return 7;
    }

    @Override
    public int getNumMidiInPorts()
    {
        return 1;
    }

    @Override
    public int getNumMidiOutPorts()
    {
        return 1;
    }

    @Override
    public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
    {
        if (platformType == PlatformType.WINDOWS || platformType == PlatformType.MAC)
        {
            list.add(new String[]{"Midi Fighter Twister"}, new String[]{"Midi Fighter Twister"});
        }
        else if (platformType == PlatformType.LINUX)
        {
            // TODO: Set the correct names of the ports for auto detection on Windows platform here
            // and uncomment this when port names are correct.
            // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
        }
    }

    @Override
    public MidiFighterTwisterExtension createInstance(final ControllerHost host)
    {
        return new MidiFighterTwisterExtension(this, host);
    }

    @Override
    public String getHelpFilePath() {
        return "Twister4Bitwig.pdf";
    }
}
