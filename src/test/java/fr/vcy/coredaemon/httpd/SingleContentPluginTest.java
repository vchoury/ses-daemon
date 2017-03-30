package fr.vcy.coredaemon.httpd;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.plugins.PluginFactory;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class SingleContentPluginTest {
    
    @BeforeClass
    public static void setUp() throws Exception {
    	TestingServer.setUp(18007);
    	TestingServer.getServer().add(PluginFactory.createPlugin("SingleContentPlugin", "/hello", "Hello World !"));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }
    @Test
    public void test() throws IOException {
        String url = TestingServer.getAdress() + "/hello";
        Requester req = Requester.get(url);
        Assert.assertEquals("GET " + url + " retourne un code inattendu", 200, req.code());
        Assert.assertEquals("GET " + url + " retourne un contenu inattendu", "Hello World !", req.body());
    }
}
