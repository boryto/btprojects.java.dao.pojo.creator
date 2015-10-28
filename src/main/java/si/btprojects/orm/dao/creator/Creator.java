package si.btprojects.orm.dao.creator;

/**
 * Created by Borut Terpinc
 */

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import si.btprojects.orm.dao.creator.lib.Helpers;
import si.btprojects.orm.dao.creator.types.DaoMetaData;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.*;
import java.util.*;

import static si.btprojects.orm.dao.creator.lib.Helpers.getTableMetaData;
import static si.btprojects.orm.dao.creator.lib.Helpers.getTableMetaDataUpdate;


public class Creator {


    String connection;
    String username;
    String password;
    String schema;
    String tableName;
    String packdao;
    String packhelpers;
    String type;
    String writedir;
    VelocityEngine ve;

    private void getArgumentsFromInput(String[] args) {

        connection = args[0]; // "jdbc:oracle:thin:@mydatabase:1521/sid";
        username = args[1]; //"test";
        password = args[2]; //"test";
        schema = args[3]; //"SC";
        tableName = args[4]; //"mytable1; mytable2";
        packdao = args[5]; //"testproject.ejb.view.model";
        packhelpers = args[6]; //".testproject.dao";
        type = args[7]; //jdbcEjb, spring, ClRmOnly
        writedir = args.length == 9 ? args[8] : System.getProperty("user.dir");


    }

    public static void main(String[] args)
            throws Exception {
        Creator instance = new Creator();
        instance.runInstance(args);
    }

    public void runInstance(String[] args) throws Exception {
        /**
         * set jdbc driver instance.
         * Jdbc driver has to be defined as dependency in build.gradle
         */
        Class.forName("oracle.jdbc.driver.OracleDriver");
        //start the creator
        getArgumentsFromInput(args);
        getVelocityEngine();
        createDaoObjects();
    }

    private void createDaoObjects() throws Exception {
        Connection dbCon = DriverManager.getConnection(connection, username, password);
        try {

            System.out.println("Starting creator.....");

            System.out.println("Generating for type " + type);
            System.out.println("package: " + packdao);
            System.out.println("schema: " + schema);

            String[] tables = tableName.split(";");
            for (String table : tables) {
                System.out.println("Creating DAO classes for table " + table + ".....");

                createDaoClasses(ve, packdao, schema, table, getTableMetaData(dbCon, table, schema), writedir);

                //row mappers for spring
                if (type.equalsIgnoreCase("ClRmOnly")) {
                    createRowMappersSpring(ve, packhelpers, packdao, schema, table, getTableMetaData(dbCon, table, schema), writedir);
                    continue;
                }

                System.out.println("Creating Dao helpers for " + table + ".....");

                //only jdbcEjb Helpers
                if (type.equalsIgnoreCase("jdbcEjb")) {
                    createDaoHelpers(ve, packhelpers, packdao, schema, table, getTableMetaData(dbCon, table, schema), getTableMetaDataUpdate(dbCon, table, schema), writedir);

                } else {
                    createRowMappersSpring(ve, packhelpers, packdao, schema, table, getTableMetaData(dbCon, table, schema), writedir);
                    createDaoHelpersSpring(ve, packhelpers, packdao, schema, table, getTableMetaData(dbCon, table, schema), getTableMetaDataUpdate(dbCon, table, schema), writedir);
                }

            }

        } finally {
            if (dbCon != null) {
                try {
                    dbCon.close();
                } catch (Exception e) {
                    System.out.println("Could not close DB connection: " + e.getMessage());
                }
            }
        }
    }

    private void getVelocityEngine() throws ClassNotFoundException {
    /*  first, get and initialize an engine  */
        if (ve == null) {
            ve = new VelocityEngine();
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            ve.init();
        }
    }


    private static void createDaoHelpersSpring(VelocityEngine ve, String packdao, String pack, String schema, String tabela, List<DaoMetaData> tableMetaData, List<DaoMetaData> tableMetaDataUpdate, String writedir) throws Exception {
        Template t = ve.getTemplate("templates/daoHelpersSpring.vm");
        VelocityContext context = new VelocityContext();

        String helpClassName = Helpers.createClassName(schema, tabela) + "DaoJdbcSpring";

        context.put("packageName", packdao);
        context.put("packageNameModel", pack);
        context.put("dbSchema", schema);
        context.put("dbTable", tabela);
        context.put("dbTableSmall", tabela.substring(0, 1) + tabela.substring(1, tabela.length()).toLowerCase());
        context.put("dbObjectName", Helpers.createClassName(schema, tabela));
        context.put("className", helpClassName);
        context.put("tableData", tableMetaData);
        context.put("tableDataUpdate", tableMetaDataUpdate);
        writeToFile(packdao, writedir, t, context, helpClassName);


    }

    private static void writeToFile(String packdao, String writedir, Template t, VelocityContext context, String helpClassName) throws Exception {
    /* now render the template into a StringWriter */
        Writer writer = null;
        //writer = new StringWriter();
        String pathextension = "/build/createdByDaoCreator/" + packdao.replace(".", "/");
        String pot = writedir + pathextension;

        if (!new File(pot).exists()) {
            createFolders(writedir, pathextension);

        }
        if (!new File(pot).exists()) {
            throw new Exception("Package " + pot + " must be created");
        }
        System.out.println("Creating helper class in " + pot + "/" + helpClassName + ".java");
        writer = new FileWriter(pot + "/" + helpClassName + ".java");
        t.merge(context, writer);
        writer.close();
    }

    private static void createFolders(String startpath, String pathextension) {

        String[] paths = pathextension.split("/");
        String path = startpath;
        for (int i=0; i<paths.length; i++) {
            path = path + "/" + paths[i];
            File theFile = new File(path);
            System.out.println("Creating folder " + path);
            boolean created =  theFile.mkdir();
            System.out.println(".. folder created ...  " + created);
        }
    }

    private static void createRowMappersSpring(VelocityEngine ve, String packhelpers, String packdao, String schema, String tabela, List<DaoMetaData> tableMetaData, String writedir) throws Exception {
        Template t = ve.getTemplate("templates/dbRowMaapper.vm");
        VelocityContext context = new VelocityContext();

        String helpClassName = Helpers.createClassName(schema, tabela) + "RowMapper";

        context.put("packageName", packhelpers);
        context.put("packageNameModel", packdao);
        context.put("dbSchema", schema);
        context.put("dbTable", tabela);
        context.put("dbTableSmall", tabela.substring(0, 1) + tabela.substring(1, tabela.length()).toLowerCase());
        context.put("dbObjectName", Helpers.createClassName(schema, tabela));
        context.put("className", helpClassName);
        context.put("tableData", tableMetaData);

        writeToFile(packdao, writedir, t, context, helpClassName);

    }

    private static void createDaoHelpers(VelocityEngine ve, String packdao, String pack, String schema, String tabela, List<DaoMetaData> tableMetaData, List<DaoMetaData> tableMetaDataUpdate, String writedir) throws Exception {

        Template t = ve.getTemplate("templates/daoHelpers.vm");
        VelocityContext context = new VelocityContext();

        String helpClassName = Helpers.createClassName(schema, tabela) + "DaoJdbc";

        context.put("packageName", packdao);
        context.put("packageNameModel", pack);
        context.put("dbSchema", schema);
        context.put("dbTable", tabela);
        context.put("dbTableSmall", tabela.substring(0, 1) + tabela.substring(1, tabela.length()).toLowerCase());
        context.put("dbObjectName", Helpers.createClassName(schema, tabela));
        context.put("className", helpClassName);
        context.put("tableData", tableMetaData);
        context.put("tableDataUpdate", tableMetaDataUpdate);

        List<DaoMetaData> reverserd = new LinkedList<DaoMetaData>(tableMetaDataUpdate);
        Collections.reverse(reverserd);
        context.put("tableDataUpdateReverse", reverserd);

        writeToFile(packdao, writedir, t, context, helpClassName);
    }

    private static void createDaoClasses(VelocityEngine ve, String packdao, String schema, String tabela, List<DaoMetaData> tableMetaData, String writedir) throws Exception {
        /*  next, get the Template  */
        Template t = ve.getTemplate("templates/dbObject.vm");
        /*  create a context and add data */
        VelocityContext context = new VelocityContext();

        context.put("packageName", packdao);
        context.put("className", Helpers.createClassName(schema, tabela));
        Random r = new Random();
        context.put("classSerialUID", r.nextLong());
        context.put("tableData", tableMetaData);

        writeToFile(packdao, writedir, t, context, Helpers.createClassName(schema, tabela).toString());
     }



}

