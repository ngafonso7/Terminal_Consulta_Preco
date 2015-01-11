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
 * @author Natanael
 */
public class Conexao {
    public static Connection getConnection() throws SQLException {

        String className = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://192.168.0.4:3306/retguarda";
        String user = "consulta";
        String password = "consulta";

        try {
            Class.forName(className);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException ex) {
            throw new SQLException(ex.getMessage());
        }
    }
}
