package si.btprojects.orm.dao.creator.types;

import si.btprojects.orm.dao.creator.lib.Helpers;

/**
 *
 * @author Borut Terpinc
 */
public class DaoMetaData {

    private String tip;
    private String tipOriginal;
    private String ime;
    
    
    public String getTipOriginal() {
        return tipOriginal;
    }

    public void setTipOriginal(String tipOriginal) {
        this.tipOriginal = tipOriginal;
    }
    

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getIme() {
        return Helpers.manipulateColumnNameForJava(ime);
    }

    public String getImeFirstCap() {
        String orig = Helpers.manipulateColumnNameForJava(ime);
        return Character.toUpperCase(orig.charAt(0)) + orig.substring(1);
    }

    public String getJavaImePolja() {
        return Character.toUpperCase(ime.charAt(0)) + ime.substring(1);
    }

    public String getdbImePolja() {
        return ime;
    }

    public String getJdbcGetTip() {
        String[] splits = tip.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "getInt";
        }

        return "get" + splits[splits.length - 1];

    }

    public String getJdbcSetTip() {
        String[] splits = tip.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "setInt";
        }

        return "set" + splits[splits.length - 1];

    }
    
    public String getJdbcGetTipOriginal() {
        String[] splits = tipOriginal.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "getInt";
        }
        if (tipOriginal.toLowerCase().contains("clob")) {
            return "getClob";
        }
        if (tipOriginal.toLowerCase().contains("blob")) {
            return "getBlob";
        }

        return "get" + splits[splits.length - 1];

    }

    public String getJdbcSetTipOriginal() {
        String[] splits = tipOriginal.split("\\.");
        if (splits[splits.length - 1].equals("Integer")) {
            return "setInt";
        }
        if (tipOriginal.toLowerCase().contains("clob")) {
            return "setClob";
        }
        if (tipOriginal.toLowerCase().contains("blob")) {
            return "setBlob";
        }
        

        return "set" + splits[splits.length - 1];

    }
    
    
    

    public String getStatement() {
        return Helpers.createRecGetStatements(tip, getImeFirstCap());
    }
    
    public String getStatementUpdate() {
        return Helpers.createRecGetStatementsUpdate(tip, getImeFirstCap());
    }

}
