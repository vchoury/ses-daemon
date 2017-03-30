package fr.vcy.coredaemon.camel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.vcy.coredaemon.TestingServer;
import fr.vcy.coredaemon.httpd.utils.Requester;

/**
 *
 * @author vchoury
 */
public class CamelTest {

    private static CamelModule camel;
    private static File wwwroot = new File("./target/cameltest/routes").getAbsoluteFile();
    private static File from = new File("./target/cameltest/receive").getAbsoluteFile();
    private static File to4 = new File("./target/cameltest/to4").getAbsoluteFile();
    private static File to5 = new File("./target/cameltest/to5").getAbsoluteFile();
    private static File to6 = new File("./target/cameltest/to6").getAbsoluteFile();

    @BeforeClass
    public static void setUp() throws Exception {
        FileUtils.deleteDirectory(wwwroot);
        FileUtils.deleteDirectory(from);
        FileUtils.deleteDirectory(to4);
        FileUtils.deleteDirectory(to5);
        FileUtils.deleteDirectory(to6);
        wwwroot.mkdirs();
        from.mkdirs();
        to4.mkdirs();
        to5.mkdirs();
        to6.mkdirs();
        TestingServer.setUp(18002);
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

    /**
     * 2 routes camel sont exécutées simultanément (pool de 10 threads)
     */    
    @Test
    public void test3() throws IOException, InterruptedException, NamingException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("camel/camel-route-test-3.xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        String r1 = baos.toString();
        baos.close();
        r1 = r1.replace("#{from}", from.getCanonicalPath());
        r1 = r1.replace("#{to4}", to4.getCanonicalPath());
        r1 = r1.replace("#{to5}", to5.getCanonicalPath());
        
        for (int i = 0; i < 5; i++) {
            File fr1 = new File(from, i + "TDECDGPCDGF.txt");
            FileUtils.write(fr1, "test" + i);
            File fr2 = new File(from, i + "TRECDGPCDGF.txt");
            FileUtils.write(fr2, "test" + i);
        }
        
        int res = Requester.put(TestingServer.getAdress() + "/camel/r3.xml").send(r1.getBytes("UTF-8")).code();
        Assert.assertEquals(200, res);
        
        for (int i = 5; i < 9; i++) {
            File fr1 = new File(from, i + "TDECDGPCDGF.txt");
            FileUtils.write(fr1, "test" + i);
            File fr2 = new File(from, i + "TRECDGPCDGF.txt");
            FileUtils.write(fr2, "test" + i);
        }
        long tick = Calendar.getInstance().getTimeInMillis();
        while (Calendar.getInstance().getTimeInMillis() - tick < 20000) {
            if (from.list().length == 0) {
                break;
            }
        }
        Assert.assertTrue(from.list().length == 0);
        Assert.assertTrue(to4.list().length > 0);
        Assert.assertTrue(to5.list().length > 0);
    }
    
    /**
     * Avec seda, possibilité de consommation parallele
     */    
    @Test 
    public void test4() throws IOException, InterruptedException, NamingException {
        Requester.delete(TestingServer.getAdress() + "/camel/r3.xml");
        
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("camel/camel-route-test-4.xml");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        String r1 = baos.toString();
        baos.close();
        r1 = r1.replace("#{from}", from.getCanonicalPath());
        r1 = r1.replace("#{to6}", to6.getCanonicalPath());
        
        for (int i = 0; i < 5; i++) {
            File fr1 = new File(from, i + "TDECDGPCDGF.txt");
            FileUtils.write(fr1, "test" + i);
        }
        
        int res = Requester.put(TestingServer.getAdress() + "/camel/r4.xml").send(r1.getBytes("UTF-8")).code();
        Assert.assertEquals(200, res);
                
        for (int i = 5; i < 9; i++) {
            File fr1 = new File(from, i + "TDECDGPCDGF.txt");
            FileUtils.write(fr1, "test" + i);
            Thread.sleep(500);
        }
        long tick = Calendar.getInstance().getTimeInMillis();
        while (Calendar.getInstance().getTimeInMillis() - tick < 20000) {
            if (from.list().length == 0) {
                break;
            }
        }
        Assert.assertTrue(from.list().length == 0);
        Assert.assertTrue(to6.list().length > 0);
    }
    
}
