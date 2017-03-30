package fr.vcy.coredaemon.httpd;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.plugins.impl.FileCounterPlugin;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class FileCounterPluginTest {
    
    private static File wwwroot = new File("./target").getAbsoluteFile();

    @BeforeClass
    public static void setUp() throws Exception {
    	TestingServer.setUp(18003);
    	TestingServer.getServer().add(new FileCounterPlugin("/filecount/target/", wwwroot));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }
    
    @Test
    public void testGet() throws IOException {
        String url = TestingServer.getAdress() + "/filecount/target/";
        Requester req = Requester.get(url);
        Assert.assertEquals("GET " + url + " retourne un code inattendu " + req.body(), 200, req.code());
    }
    
}
