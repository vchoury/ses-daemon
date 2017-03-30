package fr.vcy.coredaemon.httpd;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.plugins.impl.MockPlugin;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class MockPluginTest {

    @BeforeClass
    public static void setUp() throws Exception {
    	TestingServer.setUp(18005);
    	TestingServer.getServer().add(new MockPlugin("/test", new HttpResponse("mock"), 1000));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }
    
    @Test
    public void testInstance() throws IOException, InterruptedException {
        String url = TestingServer.getAdress() + "/test";
        Requester req = Requester.get(url);
        Assert.assertEquals("GET " + url + " retourne un code inattendu", 200, req.code());
        Assert.assertEquals("GET " + url + " retourne un contenu inattendu", "mock", req.body());
    }
}
