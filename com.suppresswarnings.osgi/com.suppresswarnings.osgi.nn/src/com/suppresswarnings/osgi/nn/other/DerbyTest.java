package com.suppresswarnings.osgi.nn.other;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DerbyTest {
    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static String protocol = "jdbc:derby:";
    String dbName = "derby";

    public static void loadDriver() {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDataFromDerby() {
        try {
            Connection conn = DriverManager.getConnection(protocol + dbName + ";user=root;password=root;create=true");
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from t_user");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
                System.out.println(resultSet.getString(2));
            }
            conn.close();
            statement.close();
            resultSet.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DerbyTest derbyTest = new DerbyTest();
        loadDriver();
        derbyTest.getDataFromDerby();
    }
}