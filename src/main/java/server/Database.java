package server;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement selectUsernameStatement;
    private static PreparedStatement selectUndeliveredMessagesStatement;
    private static PreparedStatement selectUserLoginStatement;
    private static PreparedStatement selectNameStatement;
    private static ResultSet resultSet;
    private static ResultSetMetaData metaData;
    private static int columns;
    Database (String url, String username, String password) throws SQLException{
        try {
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();

            selectUsernameStatement = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");

            String selectMessageID = "(SELECT message_id FROM undelivered_messages WHERE recipient_id = ?)";
            String query = "SELECT sender_id, pointer, content FROM messages WHERE message_id IN " + selectMessageID;
            selectUndeliveredMessagesStatement = connection.prepareStatement(query);

            query = "SELECT user_id, name FROM users WHERE username = ? AND password = crypt(?, password)";
            selectUserLoginStatement = connection.prepareStatement(query);

            query = "SELECT name FROM users WHERE username = ?";
            selectNameStatement = connection.prepareStatement(query);

            System.out.println(">>> Connected to [DATABASE] '" + url + "'.");

        } catch (SQLException e) {
            System.out.println("ERROR: [DATABASE] '" + url + "' not connected!");
            System.exit(1);
        }
    }

    static String[] fetchFirstRow (String query) {
        try {
            resultSet = statement.executeQuery(query);

            if (!resultSet.next())
                return null;

            metaData = resultSet.getMetaData();
            columns = metaData.getColumnCount();

            String[] row = new String[columns];

            for (int i = 0; i < columns; i++) {
                row[i] = resultSet.getString(i + 1);
            }

            return row;

        } catch (SQLException e) {
            return null;
        }
    }

    static User fetchUser (String username, String password) {
        try{
            selectUserLoginStatement.setString(1, username);
            selectUserLoginStatement.setString(2, password);

            ResultSet resultSet = selectUserLoginStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("name");

                return new User(id, username, name);
            }

        } catch (SQLException ignored) {}

        return null;
    }
    static Integer fetchUserID (String username) {
        String[] result = fetchFirstRow("SELECT user_id FROM Users WHERE username = '" + username + "';");

        if (result != null && result.length > 0)
            return Integer.parseInt(result[0]);
        else
            return null;

    }

    static String fetchName (String username) {
        try {
            selectNameStatement.setString(1, username);
            try (ResultSet resultSet = selectNameStatement.executeQuery()) {
                if(resultSet.next())
                    return resultSet.getString(1);
                else
                    return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    static String fetchUsername (int userID) {
        try {
            selectUsernameStatement.setInt(1, userID);
            ResultSet resultSet = selectUsernameStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getString("username");
            else
                return null;

        } catch (SQLException e) {
            return null;
        }
    }

    static int runInsertQuery (String query) {
        try {
            return statement.executeUpdate(query);

        } catch (SQLException e) {
            return 0;
        }
    }

    static boolean updateDTS (Message message) {
        String dts = "delivered_on = '" + message.deliveredTS + "'";
        String conditions = String.format("sender_id = %d AND recipient_id = %d AND pointer = %d", message.senderID, message.recipientID, message.pointer);

        int updateCount = runInsertQuery("UPDATE messages SET " + dts + " WHERE " + conditions + ";");

        return updateCount == 1;
    }

    static boolean insert (Message message) {
        String columns = "(sender_id, recipient_id, pointer, content, sent_on)";
        String variables = String.format("(%d, %d, %d, '%s', '%s')", message.senderID, message.recipientID, message.pointer, message.content, message.sentTS);

        int insertCount = runInsertQuery("INSERT INTO Messages" + columns + " VALUES" + variables + ";");

        return (insertCount == 1);
    }

    static ArrayList<Message> fetchUndeliveredMessages (int userID) {
        ArrayList<Message> messages = new ArrayList<>();

        try {
            selectUndeliveredMessagesStatement.setInt(1, userID);
            resultSet = selectUndeliveredMessagesStatement.executeQuery();

            while (resultSet.next()) {
                int senderID = resultSet.getInt("sender_id");
                Integer pointer = resultSet.getInt("pointer");
                String content = resultSet.getString("content");

                String senderUsername = fetchUsername(senderID);

                Message message = new Message(senderID, senderUsername, pointer, content);

                messages.add(message);
            }

        } catch (SQLException ignored) {}

        return messages;
    }

    public void close () {
        try {
            if (resultSet != null)
                resultSet.close();

            if (statement != null)
                statement.close();

            if (connection != null)
                connection.close();

        } catch (SQLException e) {
            System.out.println("ERROR: Unable to close Database.");
        }
    }
}

