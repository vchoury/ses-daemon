package fr.vcy.coredaemon.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author vchoury
 */
public class JndiDaoManager extends DaoManager {
    
    private DataSource datasource;
    private AtomicInteger nbConnections = new AtomicInteger();
    private ThreadLocal<Connection> connection = new ThreadLocal<Connection>();
    
    private static DaoManager instance;
    
    private JndiDaoManager() { }
    
    public static synchronized DaoManager getInstance(String jndi) throws SQLException {
        if (instance == null) {
            instance = new JndiDaoManager();
            instance.createDataSource(jndi, null, null);
        }
        return instance;
    }
    
    @Override
    protected void createDataSource(String url, String username, String password) throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            this.datasource = (DataSource) ctx.lookup(url);
        } catch (NamingException ex) {
            throw new SQLException(ex);
        }
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
        
    }

}
