package fr.vcy.coredaemon.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author vchoury
 */
public class ThreadPoolDaoManager extends DaoManager {
    
    private BasicDataSource datasource;
    private AtomicInteger nbConnections = new AtomicInteger();
    private ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
    
    private static DaoManager instance;
    
    private ThreadPoolDaoManager() { }
    
    public static synchronized DaoManager getInstance(String url, String username, String password) throws SQLException {
        if (instance == null) {
            instance = new ThreadPoolDaoManager();
            instance.createDataSource(url, username, password);
        }
        return instance;
    }
    
    @Override
    protected void createDataSource(String url, String username, String password) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(url);
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("select 1 from dual");
        this.datasource = ds;
    }
    
    @Override
    protected Connection internalGetConnection() throws SQLException {
        Connection conn = connection.get();
        if (conn == null || conn.isClosed()) {
            connection.set(datasource.getConnection());
            nbConnections.incrementAndGet();
        }
        return connection.get();
    }
    
    /**
     * @return le nombre de connexion paralleles
     */
    public int getNbConnections() {
        return nbConnections.get();
    }
    
    /**
     * Ferme la connexion associée au thread courant
     * @throws SQLException 
     */
    @Override
    public void closeConnection(Connection conn) throws SQLException {
        conn = connection.get();
        if (conn != null && !conn.isClosed()) {
            conn.close();
            connection.remove();
            nbConnections.decrementAndGet();
        }
    }
    
    /**
     * Ferme la connexion associée au thread courant
     * @throws SQLException 
     */
    @Override
    public void closeDatasource() throws SQLException {
        datasource.close();
    }

}
