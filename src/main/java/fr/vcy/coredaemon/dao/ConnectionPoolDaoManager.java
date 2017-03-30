package fr.vcy.coredaemon.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

//import oracle.jdbc.pool.OracleConnectionPoolDataSource;

/**
 *
 * @author vchoury
 */
public class ConnectionPoolDaoManager extends DaoManager {
    
    private ConnectionPoolDataSource poolDataSource;
    private PooledConnection pooledConnection;

    private static DaoManager instance;
    
    private ConnectionPoolDaoManager() { }
    
    public static synchronized DaoManager getInstance(String url, String username, String password) throws SQLException {
        if (instance == null) {
            instance = new ConnectionPoolDaoManager();
            instance.createDataSource(url, username, password);
        }
        return instance;
    }
    
    @Override
    protected void createDataSource(String url, String username, String password) throws SQLException {
//        poolDataSource = new OracleConnectionPoolDataSource();
//        poolDataSource.setURL(url);
//        poolDataSource.setUser(username);
//        poolDataSource.setPassword(password);
        pooledConnection = poolDataSource.getPooledConnection();
    }

    @Override
    protected Connection internalGetConnection() throws SQLException {
        return pooledConnection.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Override
    public void closeDatasource() throws SQLException {
        pooledConnection.close();
//        poolDataSource.close();
    }
    
}
