package fr.vcy.coredaemon.httpd;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.plugins.impl.MonitorPlugin;
import fr.vcy.coredaemon.httpd.utils.Requester;
import fr.vcy.coredaemon.httpd.utils.Requester.HttpRequestException;
import fr.vcy.coredaemon.services.ProcessMonitor;

/**
 *
 * @author vchoury
 */
public class MonitorPluginTest {
    
    @BeforeClass
    public static void setUp() throws Exception {
    	TestingServer.setUp(18006);
		ProcessMonitor pm = new ProcessMonitor();
		TestingServer.getServer().setProcessMonitor(pm);
    	TestingServer.getServer().add(new MonitorPlugin());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }
    
    @Test
    public void testJvm() throws IOException, InterruptedException {
        String monitor = TestingServer.getAdress() + "/monitor";
        Requester req = Requester.get(monitor);
        Assert.assertEquals("GET " + monitor + " retourne un code inattendu " + req.body(), 200, req.code());
        String jvm = TestingServer.getAdress() + "/monitor/jvm";
        req = Requester.get(jvm);
        Assert.assertEquals("GET " + jvm + " retourne un code inattendu " + req.body(), 200, req.code());
    }
    
    @Ignore("Permet de vérifier le parallelisme")
    @Test
    public void testInstances() throws IOException, InterruptedException {
        String monitor = TestingServer.getAdress() + "/monitor";
        final String mock = TestingServer.getAdress() + "/mock";
        Runnable r = new Runnable() {
            public void run() {
                try {
                    Requester.get(mock);
                } catch (HttpRequestException ex) {
                    ex.printStackTrace();
                }
            }
        };
        for (int i = 0; i<5; i++) {
            Thread t = new Thread(r);
            t.start();
            Thread.sleep(1000);
            System.out.println(Requester.get(monitor).body());
            Thread.sleep(1000);
        }
        for (int i = 0; i<5; i++) {
            Thread.sleep(1000);
            System.out.println(Requester.get(monitor).body());
        }
    }
}
