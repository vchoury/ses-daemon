package fr.vcy.coredaemon.services;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.HttpResponse;
import fr.vcy.coredaemon.httpd.plugins.impl.MockPlugin;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class ManagementTest {

	@BeforeClass
	public static void setUp() throws Exception {
		TestingServer.setUp(18008);
	}

    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }

	@Test
	public void test() throws InterruptedException {
		ProcessMonitor pm = new ProcessMonitor();
		pm.startMonitoring(this, 1000);
		try {
			Thread.sleep(5000);
			Assert.fail("Le timeout aurait du interrompre le test!");
			pm.stopMonitoring(this);
		} catch (InterruptedException ex) {
			Thread.sleep(2000); // J'attends volontairement 2sec pour que le
								// process monitor fasse un nouveau tour. au cas
								// où.
		}
		Assert.assertEquals(0, pm.getProcesses().size());
	}

	@Test
	public void testServer() throws Exception {
		ProcessMonitor pm = new ProcessMonitor();
		TestingServer.getServer().setProcessMonitor(pm);
		TestingServer.getServer().add(new MockPlugin("/test", new HttpResponse("mock"), 99000));
		String url = TestingServer.getAdress() + "/test";
		Requester req = Requester.get(url);
		Assert.assertEquals("GET " + url + " retourne un code inattendu",
				200, req.code());
        Assert.assertEquals("GET " + url + " retourne un contenu inattendu", "Interrupted", req.body());
	}
}
