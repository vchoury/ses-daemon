package fr.vcy.coredaemon.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vchoury
 */
public abstract class DaoManager {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(DaoManager.class.getName());
    
    protected abstract void createDataSource(String url, String username, String password) throws SQLException;
            
    protected abstract Connection internalGetConnection() throws SQLException;
    
    public abstract void closeConnection(Connection conn) throws SQLException;
    
    public abstract void closeDatasource() throws SQLException;
    
    /**
     * @return AutoCommit connection
     * @throws SQLException 
     */
    public Connection getConnection() throws SQLException {
        Connection conn = internalGetConnection();
        conn.setAutoCommit(true);
        return conn;
    }

    /**
     * @return Transactionnal connection
     * @throws SQLException 
     */
    public Connection getTxConnection() throws SQLException {
        Connection conn = internalGetConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * @return Read Only connection
     * @throws SQLException 
     */
    public Connection getReadOnlyConnection() throws SQLException {
        Connection conn = internalGetConnection();
        conn.setReadOnly(true);
        return conn;
    }
    
    /**
     * select sysdate from dual
     * @throws SQLException 
     */
    public void testConnection() throws SQLException {
        LOGGER.debug("Test DB");
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getReadOnlyConnection();
            stmt = conn.createStatement();
            stmt.execute("select sysdate from dual");
        } finally {
            closeStatement(null, stmt);
            closeConnection(conn);
        }

    }
    
    /**
     * Ferme le statement
     * @param rset
     * @param stmt 
     */
    public void closeStatement(ResultSet rset, Statement stmt) {
        try {
            if (rset != null) {
                rset.close();
            }
        } catch (Exception e) {
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            closeDatasource();
        } catch (Exception ex) { 
            LOGGER.error(null, ex);
        } finally {
            super.finalize();
        }
    }

}
