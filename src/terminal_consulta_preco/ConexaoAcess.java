/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package terminal_consulta_preco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author ngafo_000
 */
public class ConexaoAcess {
    public static Connection getConnection() throws SQLException {

        String className = "sun.jdbc.odbc.JdbcOdbcDriver";
        
        String database = "jdbc:odbc:dadosNF";
        

        try {
            Class.forName(className);
            return DriverManager.getConnection(database);
        } catch (ClassNotFoundException ex) {
            throw new SQLException(ex.getMessage());
        }
    }
}
