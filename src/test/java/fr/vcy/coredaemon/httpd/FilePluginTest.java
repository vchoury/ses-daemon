package fr.vcy.coredaemon.httpd;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.plugins.impl.FilePlugin;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class FilePluginTest {
    
    private static File wwwroot = new File("./target/fileplugintest").getAbsoluteFile();

    @BeforeClass
    public static void setUp() throws Exception {
        wwwroot.delete();
        wwwroot.mkdir();
        File test1 = new File(wwwroot, "test1.txt");
        test1.createNewFile();
        FileWriter fw = new FileWriter(test1);
        fw.append("this is a test");
        fw.close();
        TestingServer.setUp(18004);
        TestingServer.getServer().add(new FilePlugin("files", "Test du serveur de fichiers", wwwroot));
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	TestingServer.tearDown();
    }
    
    @Test
    public void testGetDir() throws IOException {
        String url = TestingServer.getAdress() + "/files";
        Requester req = Requester.get(url);
        Assert.assertEquals("GET " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertTrue("GET " + url + " retourne un contenu inattendu : " + body, body.contains("test1.txt"));
    }
    
    @Test
    public void testGetFile() throws IOException {
        String url = TestingServer.getAdress() + "/files/test1.txt";
        Requester req = Requester.get(url);
        Assert.assertEquals("GET " + url + " retourne un code inattendu", 200, req.code());
        Assert.assertEquals("GET " + url + " retourne un contenu inattendu", "this is a test", req.body());
    }

    @Test
    public void testCreateFile() throws IOException {
        String url = TestingServer.getAdress() + "/files/test2.txt";
        File f2 = new File(wwwroot, "test2.txt");
        if (f2.exists()) {
            f2.delete();
        }
        Requester req = Requester.put(url).send("this is à @create test\nHmmm!");
        Assert.assertEquals("PUT " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertTrue("PUT " + url + " retourne un contenu inattendu : " + body, body.contains("test2.txt"));
        Assert.assertTrue(f2.getCanonicalPath() + " is not a file.", f2.isFile());
        String f2Content = FileUtils.readFileToString(f2, "UTF-8");
        Assert.assertEquals(f2.getCanonicalPath() + " wrong content", "this is à @create test\nHmmm!", f2Content);
    }
    
    @Test
    public void testCreateDir() throws IOException {
        String url = TestingServer.getAdress() + "/files/rep";
        Requester req = Requester.put(url);
        Assert.assertEquals("PUT " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertTrue("PUT " + url + " retourne un contenu inattendu : " + body, body.contains("rep"));
        File rep = new File(wwwroot, "rep");
        Assert.assertTrue(rep.getCanonicalPath() + " is not a directory.", rep.isDirectory());
        
        url = TestingServer.getAdress() + "/files/rep/test2.txt";
        File f2 = new File(rep, "test2.txt");
        if (f2.exists()) {
            f2.delete();
        }
        req = Requester.put(url).send("this is à @create DIR test\nHmmm!");
        Assert.assertEquals("PUT " + url + " retourne un code inattendu", 200, req.code());
        body = req.body();
        Assert.assertTrue("PUT " + url + " retourne un contenu inattendu : " + body, body.contains("rep/test2.txt"));
        Assert.assertTrue(f2.getCanonicalPath() + " is not a file.", f2.isFile());
        String f2Content = FileUtils.readFileToString(f2, "UTF-8");
        Assert.assertEquals(f2.getCanonicalPath() + " wrong content", "this is à @create DIR test\nHmmm!", f2Content);
    }
    
    @Test
    public void testModifyFile() throws IOException {
        String url = TestingServer.getAdress() + "/files/test5.txt";
        File f5 = new File(wwwroot, "test5.txt");
        f5.createNewFile();
        FileWriter fw = new FileWriter(f5);
        fw.append("you're alive!");
        fw.close();
        Requester req = Requester.put(url).send("this is à @modify test\nHmmm!");
        Assert.assertEquals("PUT " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertTrue("PUT " + url + " retourne un contenu inattendu : " + body, body.contains("test5.txt"));
        Assert.assertTrue(f5.getCanonicalPath() + " does not exist.", f5.isFile());
        String f5Content = FileUtils.readFileToString(f5, "UTF-8");
        Assert.assertEquals(f5.getCanonicalPath() + " wrong content", "this is à @modify test\nHmmm!", f5Content);
    }
    
    @Test
    public void testDeleteFile() throws IOException {
        String url = TestingServer.getAdress() + "/files/test3.txt";
        File f3 = new File(wwwroot, "test3.txt");
        f3.createNewFile();
        FileWriter fw = new FileWriter(f3);
        fw.append("you're gonna die...");
        fw.close();
        Requester req = Requester.delete(url);
        Assert.assertEquals("DELETE " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertFalse("DELETE " + url + " retourne un contenu inattendu : " + body, body.contains("test3.txt"));
        Assert.assertFalse(f3.getPath() + " not deleted.", f3.exists());
    }
    
    @Test
    public void testDeleteDir() throws IOException {
        URL url = new URL(TestingServer.getAdress() + "/files/rep2");
        File f4 = new File(wwwroot, "rep2/test4");
        f4.getParentFile().mkdirs();
        f4.createNewFile();
        FileWriter fw = new FileWriter(f4);
        fw.append("you're gonna die...");
        fw.close();
        Requester req = Requester.delete(url);
        Assert.assertEquals("DELETE " + url + " retourne un code inattendu", 200, req.code());
        String body = req.body();
        Assert.assertFalse("DELETE " + url + " retourne un contenu inattendu : " + body, body.contains("rep2"));
        Assert.assertFalse(f4.getPath() + " not deleted.", f4.exists());
        Assert.assertFalse(f4.getParentFile().getPath() + " not deleted.", f4.getParentFile().exists());
    }
}
