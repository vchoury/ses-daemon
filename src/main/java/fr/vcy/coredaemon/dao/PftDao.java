package fr.vcy.coredaemon.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
//import oracle.jdbc.OracleTypes;

/**
 * Class de démonstration dao
 * @author VCHOURY
 */
public class PftDao {

    public static final String DB_URL = "ses.db.tcl.url";
    public static final String DB_USERNAME = "ses.db.tcl.username";
    public static final String DB_PASSWORD = "ses.db.tcl.password";
    
    protected DaoManager daoManager;
    
    public PftDao() throws SQLException {
        this(ThreadPoolDaoManager.getInstance(System.getProperty(DB_URL), System.getProperty(DB_USERNAME), System.getProperty(DB_PASSWORD)));
    }
    
    public PftDao(DaoManager daoManager) {
        this.daoManager = daoManager;
    }
    
    public Date demo(String codeBm, String nomFichier, String comment) throws SQLException {
        String sql = "call PI_PFT_FICHIER (?, ?, ?, ?)";
        CallableStatement cs = null;
        Connection conn = null;
        try {
            conn = daoManager.getConnection();
            cs = conn.prepareCall(sql);
            cs.setString(1, StringUtils.abbreviate(codeBm, 10));
            cs.setString(2, StringUtils.abbreviate(nomFichier, 100));
            cs.setString(3, StringUtils.abbreviate(comment, 300));
            cs.registerOutParameter(4, Types.TIMESTAMP);
            cs.executeQuery();
            return cs.getTimestamp(4);
        } finally {
            daoManager.closeStatement(null, cs);
            daoManager.closeConnection(conn);
        }
    }

}
