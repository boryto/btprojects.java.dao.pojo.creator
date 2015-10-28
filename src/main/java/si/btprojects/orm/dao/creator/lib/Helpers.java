package si.btprojects.orm.dao.creator.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import si.btprojects.orm.dao.creator.types.DaoMetaData;

/**
 *
 * @author Borut Terpinc
 */
public class Helpers {


    /***
     *
     * Get table meta data
     *
     * @param con -- connection
     * @param table -- table name
     * @param schema -- schema name
     * @return -- list of MetaData objects
     * @throws SQLException
     */

    public static List<DaoMetaData> getTableMetaData(Connection con, String table, String schema) throws SQLException {
        Statement stm = con.createStatement();
        List<DaoMetaData> daoMd = new ArrayList<DaoMetaData>();
        try {
            ResultSet rs = stm.executeQuery("Select * from " + schema + "." + table + " fetch first row only");
            try {
                ResultSetMetaData metadat = rs.getMetaData();
                int columnCount = metadat.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    DaoMetaData ent = new DaoMetaData();
                    ent.setIme(metadat.getColumnName(i));
                    ent.setTip(Helpers.manipulateJavaType(metadat.getColumnClassName(i)));
                    ent.setTipOriginal(Helpers.manipulateJavaTypeOriginal(metadat.getColumnClassName(i)));
                    daoMd.add(ent);
                }
            } finally {
                rs.close();
            }
        } finally {
            stm.close();
        }
        return daoMd;
    }

    /***
     *
     * Get table meta data for updates
     *
     * @param con -- connection
     * @param table -- table name
     * @param schema -- schema name
     * @return -- list of MetaData objects
     * @throws SQLException
     */

    public static List<DaoMetaData> getTableMetaDataUpdate(Connection con, String table, String schema) throws SQLException {
        Statement stm = con.createStatement();
        List<DaoMetaData> daoMd = new ArrayList<DaoMetaData>();
        try {
            ResultSet rs = stm.executeQuery("Select * from " + schema + "." + table + " fetch first row only");
            try {
                ResultSetMetaData metadat = rs.getMetaData();
                int columnCount = metadat.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    DaoMetaData ent = new DaoMetaData();
                    ent.setIme(metadat.getColumnName(i));
                    ent.setTip(Helpers.manipulateJavaTypeUpdate(metadat.getColumnClassName(i)));
                    ent.setTipOriginal(metadat.getColumnClassName(i));
                    daoMd.add(ent);
                }
            } finally {
                rs.close();
            }
        } finally {
            stm.close();
        }
        return daoMd;
    }


    //manipulation of java type -- sql date and timestamp is converted to java.util.date
    public static String manipulateJavaType(String columnClassName) {
        if (columnClassName.toLowerCase().contains("date")) {
            return "java.util.Date";
        }
        if (columnClassName.toLowerCase().contains("timestamp")) {
            return "java.util.Date";
        }
        if (columnClassName.toLowerCase().contains("clob")) {
            return "javax.sql.rowset.serial.SerialClob";
        }
        if (columnClassName.toLowerCase().contains("blob")) {
            return "javax.sql.rowset.serial.SerialBlob";
        }

        return columnClassName;
    }

    public static String manipulateJavaTypeOriginal(String columnClassName) {
        if (columnClassName.toLowerCase().contains("timestamp")) {
            return "java.sql.Timestamp";
        }
        if (columnClassName.toLowerCase().contains("clob")) {
            return "javax.sql.rowset.serial.SerialClob";
        }
        if (columnClassName.toLowerCase().contains("blob")) {
            return "javax.sql.rowset.serial.SerialBlob";
        }

        return columnClassName;
    }
     

    public static String manipulateJavaTypeUpdate(String columnClassName) {

        if (columnClassName.toLowerCase().contains("clob")) {
            return "javax.sql.rowset.serial.SerialClob";
        }
        if (columnClassName.toLowerCase().contains("blob")) {
            return "javax.sql.rowset.serial.SerialBlob";
        }
        

        return columnClassName;
    }

    //how we create names for Java objects
    public static String manipulateColumnNameForJava(String colName) {
        colName = colName.toLowerCase();
        String[] splited = colName.split("_");

        if (splited.length == 1) {
            return colName;
        }

        colName = splited[0];

        for (int i = 1; i < splited.length; i++) {
            colName += Character.toUpperCase(splited[i].charAt(0)) + splited[i].substring(1);
        }

        return colName;
    }

    //creating geters
    public static String getMethodByType(String javaType) {
        String[] splits = javaType.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "getInt";
        }
        return "get" + splits[splits.length - 1];
    }


    //creating setters
    public static String setMethodByType(String javaType) {
        String[] splits = javaType.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "setInt";
        }

        return "set" + splits[splits.length - 1];

    }

    //crating class name
    public static Object createClassName(String schema, String tabela) {
        return Character.toUpperCase(schema.charAt(0)) + schema.substring(1).toLowerCase()
                + Character.toUpperCase(tabela.charAt(0)) + tabela.substring(1).toLowerCase();
    }

    //crating variable name
    public static Object createVariableName(String schema, String tabela) {
        return schema.toLowerCase()
                + Character.toUpperCase(tabela.charAt(0)) + tabela.substring(1).toLowerCase();
    }

    
    //changing of types
    public static String createRecGetStatements(String tip, String imeFirstCap) {
        StringBuilder sb = new StringBuilder();
        if (tip.toLowerCase().contains("timestamp")) {
            sb.append("rec.get").append(imeFirstCap).append("() == null ? null : new java.sql.Date (rec.get").append(imeFirstCap).append("().getTime())");
        } else {
            sb.append("rec.get").append(imeFirstCap).append("()");
        }
        return sb.toString();
    }


    //changing of types
    public static String createRecGetStatementsUpdate(String tip, String imeFirstCap) {
        StringBuilder sb = new StringBuilder();
        if (tip.toLowerCase().contains("timestamp")) {
            sb.append("rec.get").append(imeFirstCap).append("() == null ? null : new java.sql.Timestamp(rec.get").append(imeFirstCap).append("().getTime())");
        } else if (tip.contains("Date")) {
            sb.append("rec.get").append(imeFirstCap).append("() == null ? null : new java.sql.Date(rec.get").append(imeFirstCap).append("().getTime())");
        } else if (tip.contains("Integer") || tip.contains("Long") || tip.contains("Float") || tip.contains("Double")) {
            sb.append("rec.get").append(imeFirstCap).append("() == null ? 0 : rec.get").append(imeFirstCap).append("()");
        } else {
            sb.append("rec.get").append(imeFirstCap).append("()");
        }
        return sb.toString();
    }



}
