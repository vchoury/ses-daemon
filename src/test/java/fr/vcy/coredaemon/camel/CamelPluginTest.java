package fr.vcy.coredaemon.camel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class CamelPluginTest {
    
    private static CamelModule camel;
    private static File wwwroot = new File("./target/camelplugintest/routes").getAbsoluteFile();
    private static File from = new File("./target/camelplugintest/receive").getAbsoluteFile();
    private static File to1 = new File("./target/camelplugintest/to1").getAbsoluteFile();
    private static File to2 = new File("./target/camelplugintest/to2").getAbsoluteFile();
    private static File to3 = new File("./target/camelplugintest/to3").getAbsoluteFile();
    private static File to4 = new File("./target/camelplugintest/to4").getAbsoluteFile();
    private static File to5 = new File("./target/camelplugintest/to5").getAbsoluteFile();

    @BeforeClass
    public static void setUp() throws Exception {
        FileUtils.deleteDirectory(wwwroot);
        FileUtils.deleteDirectory(from);
        FileUtils.deleteDirectory(to1);
        FileUtils.deleteDirectory(to2);
        FileUtils.deleteDirectory(to4);
        FileUtils.deleteDirectory(to5);
        wwwroot.mkdirs();
        from.mkdirs();
        to1.mkdirs();
        to2.mkdirs();
        to4.mkdirs();
        to5.mkdirs();
        TestingServer.setUp(18001);
        FileUtils.deleteDirectory(TestingServer.getCfg().getCamelDir());
        camel = new CamelModule(TestingServer.getCfg().getCamelDir());
        camel.init(TestingServer.getCfg());
        camel.start(TestingServer.getCfg());
        TestingServer.getServer().add(camel.getCamelPlugin());
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
    	camel.stop(TestingServer.getCfg());
    	TestingServer.tearDown();
    }
    
    @Test
    public void testSimple() throws IOException, InterruptedException {
        String r1 = "<routes xmlns=\"http://camel.apache.org/schema/spring\">\n"
            + "  <route id=\"r1\">\n"
            + "    <from uri=\"file:" + from.getCanonicalPath() + "?include=.*(TDECDGPCDGF\\.|TRECDG).*&amp;preMove=../archive/${date:now:yyyyMMdd}/${file:name}\" />\n"
                // + "&amp;recursive=true&amp;readLock=changed&amp;keepLastModified=true&amp;fileExist=Fail&amp;include=.*TDECDGPCDGF.*&amp;moveFailed=../erreur/10_Receive/${file:name.noext}-${date:now:yyyyMMddHHmmssSSS}.${file:ext}\" />\n"
            + "    <to uri=\"file:" + to4.getCanonicalPath() + "\" />\n"
            + "  </route>\n"
            + "</routes>";
        
        int res = Requester.put(TestingServer.getAdress() + "/camel/r1.xml").send(r1.getBytes("UTF-8")).code();
        Assert.assertEquals(200, res);
        
        File fr1 = new File(from, "F1TDECDGPCDGF.txt");
        fr1.createNewFile();
        Thread.sleep(2000);
        Assert.assertFalse(fr1.exists());
        File fs1 = new File(to4, "F1TDECDGPCDGF.txt");
        Assert.assertTrue(fs1.exists());
    }
    
    /**
     * Possibilité de load-balancing
     */    
    @Test
    public void testLoadBalancer() throws IOException, InterruptedException, NamingException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("camel/camel-route-test.xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        String r1 = baos.toString();
        baos.close();
        System.setProperty("from", from.getCanonicalPath());
        r1 = r1.replace("#{to1}", to1.getCanonicalPath());
        r1 = r1.replace("#{to2}", to2.getCanonicalPath());
        
        int res = Requester.put(TestingServer.getAdress() + "/camel/r1.xml").send(r1.getBytes("UTF-8")).code();
        Assert.assertEquals(200, res);
        
        List<File> fs = new ArrayList<File>();
        for (int i = 0; i < 5; i++) {
            File fr1 = new File(from, i + "PDBOR.txt");
            FileUtils.write(fr1, "test" + i);
            fs.add(fr1);
        }
        long tick = Calendar.getInstance().getTimeInMillis();
        while (Calendar.getInstance().getTimeInMillis() - tick < 60000) {
            if (from.list().length == 0) {
                break;
            }
        }
        for (File f : fs) {
            Assert.assertFalse(f.getAbsolutePath() + " exists!", f.exists());
        }
        Assert.assertTrue(to1.list().length > 0);
        Assert.assertTrue(to2.list().length > 0);
        
    }
    
    /**
     * Fonctionne avec les disques réseau
     */    
    @Ignore("\\\\d20845\\public\\tmp marche uniquement sur les machines windows")
    @Test
    public void test2() throws IOException, InterruptedException, NamingException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("camel/camel-route-test-2.xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        String r1 = baos.toString();
        baos.close();
        r1 = r1.replace("#{from}", from.getCanonicalPath());
        
        int res = Requester.put(TestingServer.getAdress() + "/camel/r2.xml").send(r1.getBytes("UTF-8")).code();
        Assert.assertEquals(200, res);
        
        for (int i = 0; i < 5; i++) {
            File fr1 = new File(from, i + "PDBOR.txt");
            FileUtils.write(fr1, "test" + i);
        }
        long tick = Calendar.getInstance().getTimeInMillis();
        while (Calendar.getInstance().getTimeInMillis() - tick < 20000) {
            if (from.list().length == 0) {
                break;
            }
        }
        Assert.assertTrue(from.list().length == 0);
        Assert.assertTrue(to3.list().length > 0);
    }
    
}
