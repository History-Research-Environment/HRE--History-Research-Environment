package hre.pf4j;
/**
 * 
 */

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Plugin;
import org.pf4j.PluginAlreadyLoadedException;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

public class MenuPluginTool extends Plugin {

    @SuppressWarnings("deprecation")
	public MenuPluginTool(PluginWrapper wrapper) {
        super(wrapper);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void start() throws PluginAlreadyLoadedException {
        System.out.println(" -> " + getClass().getName() + ".started");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(wrapper.getRuntimeMode())) {
            System.out.println(StringUtils.upperCase(" " + getClass().getName() + ".Runtime"));
        }
    }

    @Override
    public void stop() throws PluginRuntimeException {
    	System.out.println(" -> " + getClass().getName() + ".stopped");
    }

    @Override
    public void delete() throws PluginRuntimeException {
    	System.out.println(" -> " + getClass().getName() + ".deleted");
    }

}

