package client;

import java.sql.*;
import java.util.ArrayList;

public class ChatRepository {
    Connection connection;
    PreparedStatement insertNewChatStatement, selectChatIDStatement;
    Statement statement;

    ChatRepository (String url) {
        try {
            connection = DriverManager.getConnection(url);

            statement = connection.createStatement();

            statement.executeUpdate("DROP TABLE chats");

            try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "chats", null)) {
                if (!resultSet.next()) {
                    String query = "CREATE TABLE chats(" +
                            "chat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "username VARCHAR(30) UNIQUE NOT NULL," +
                            "name VARCHAR(50) NOT NULL," +
                            "message_pointer INTEGER NOT NULL)";

                    statement.executeUpdate(query);
                }
            }
            statement.close();

            insertNewChatStatement = connection.prepareStatement("INSERT INTO chats(username, name, message_pointer) VALUES(?, ?, ?)");
            selectChatIDStatement = connection.prepareStatement("SELECT chat_id FROM chats WHERE username = ?");

        } catch (SQLException e) {
            System.out.println("ERROR: Unable to create/open repository!");
            System.exit(1);
        }
    }

     ArrayList<Chat> fetchChatList() {
        ArrayList<Chat> chatList = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM chats")) {
            while (resultSet.next()) {
                int id = resultSet.getInt("chat_id");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                int pointer = resultSet.getInt("message_pointer");

                chatList.add(new Chat(id, username, name, pointer));
            }

        } catch (SQLException e) {}

        return chatList;
    }

    Chat getNewChat (String username, String name, int messagePointer) {
        try {
            insertNewChatStatement.setString(1, username);
            insertNewChatStatement.setString(2, name);
            insertNewChatStatement.setInt(3, messagePointer);

            if (insertNewChatStatement.executeUpdate() != 1)
                return null;

            selectChatIDStatement.setString(1, username);

            try (ResultSet resultSet = selectChatIDStatement.executeQuery()) {
                if(resultSet.next()) {
                    int id = resultSet.getInt("chat_id");

                    return new Chat(id, username, name, messagePointer);
                }
            }

        } catch (SQLException e) {}

        return null;
    }

    void close() {
        try {
            statement.close();
            insertNewChatStatement.close();
            selectChatIDStatement.close();
        } catch (SQLException e) {

        }

    }
}
