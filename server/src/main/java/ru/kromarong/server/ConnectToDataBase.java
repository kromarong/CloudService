package ru.kromarong.server;

import java.sql.*;


public class ConnectToDataBase {
    private Connection connection;
    public ConnectToDataBase() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:server/ClientDataBase.db");
    }

    public boolean checkUser(String login, String password) throws SQLException {
        boolean result = false;

        PreparedStatement findUser = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
        findUser.setString(1, login);
        findUser.setString(2, password);
        ResultSet rs = findUser.executeQuery();

        if (rs.next()) {
            result = true;
        }

        return result;
    }

    public boolean createNewUser(String login, String password) throws SQLException {
        PreparedStatement check = connection.prepareStatement("SELECT * FROM users WHERE login = ?");
        check.setString(1, login);
        ResultSet rs = check.executeQuery();

        if (rs.next()) {
            return false;
        }
        PreparedStatement createUser = connection.prepareStatement("INSERT INTO users (login, password) values (?,?)");
        createUser.setString(1, login);
        createUser.setString(2, password);
        createUser.executeUpdate();

        return true;

    }
}
