package fr.vcy.coredaemon.httpd;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import fr.vcy.coredaemon.httpd.plugins.impl.ProcesserPlugin;

public class RequesterPluginTest {

    @Test
    public void testInstance() throws IOException, InterruptedException {
    	HttpRequest req = new HttpRequest("/process", "POST");
    	req.addParms("method", "POST");
    	req.addParms("do", "shutdown");
    	TestProcesserPlugin plugin = new TestProcesserPlugin();
    	req.accept(plugin);
        Assert.assertEquals("Commande inatendue", "shutdown", plugin.cmdCalled);
    }
    

	private class TestProcesserPlugin extends ProcesserPlugin {
		public String cmdCalled = "";
		@Override
		public boolean doProcess(String cmd) {
			cmdCalled += cmd;
			return true;
		}
		
	};
}
