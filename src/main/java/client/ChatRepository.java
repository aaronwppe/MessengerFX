package client;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class ChatRepository {
    Connection connection;
    PreparedStatement insertNewChatStatement, insertMessageStatement, insertSTSStatement;
    PreparedStatement selectChatIDStatement, selectMessagesStatement;
    Statement statement;
    boolean isConnected;

    ChatRepository (String url) {
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();

            /*statement.executeUpdate("DROP TABLE messages");
            statement.executeUpdate("DROP TABLE chats");*/

            statement.execute("PRAGMA foreign_keys = ON");

            //create main 'chats' and 'messages' table if required!
            setupChatSchemaIfRequired();

            selectChatIDStatement = connection.prepareStatement("SELECT chat_id FROM chats WHERE username = ?");
            selectMessagesStatement = connection.prepareStatement("SELECT * FROM messages WHERE chat_id = ?");

            insertNewChatStatement = connection.prepareStatement("INSERT INTO chats(username, name) VALUES(?, ?)");
            insertMessageStatement = connection.prepareStatement("INSERT INTO messages(chat_id, message_pointer, content) VALUES (?, ?, ?)");
            insertSTSStatement = connection.prepareStatement("UPDATE messages SET sent_on = ? WHERE chat_id = ? AND message_pointer = ?");

            isConnected = true;

        } catch (SQLException e) {
            isConnected = false;
        }
    }

    void setupChatSchemaIfRequired() throws SQLException{
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "chats", null)) {
            if (!resultSet.next()) {
                String query = "CREATE TABLE chats(" +
                                "chat_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "username VARCHAR(30) UNIQUE NOT NULL," +
                                "name VARCHAR(50) NOT NULL)";
                statement.executeUpdate(query);

                query = "CREATE TABLE messages(" +
                        "message_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "chat_id INTEGER," +
                        "message_pointer INTEGER," +
                        "content TEXT," +
                        "sent_on TIMESTAMP," +
                        "delivered_on TIMESTAMP," +
                        "FOREIGN KEY(chat_id) REFERENCES chats(chat_id))";
                statement.executeUpdate(query);

                query = "CREATE UNIQUE INDEX unqiue_pointer_index " +
                        "ON messages(chat_id, message_pointer)";
                statement.executeUpdate(query);
            }
        }
    }

     ArrayList<Chat> fetchChatList() {
        ArrayList<Chat> chatList = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM chats")) {
            while (resultSet.next()) {
                int id = resultSet.getInt("chat_id");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");

                Chat chat = new Chat(id, username, name);
                chat.messageList = fetchMessageList(id);

                chatList.add(chat);
            }

        } catch (SQLException ignored) {}

        return chatList;
    }

    LinkedList<Message> fetchMessageList(int chatID) {
        LinkedList<Message> messageList = new LinkedList<>();
        try {
            selectMessagesStatement.setInt(1, chatID);

            try (ResultSet resultSet = selectMessagesStatement.executeQuery()) {
                while (resultSet.next()) {
                    int pointer = resultSet.getInt("message_pointer");
                    String content = resultSet.getString("content");
                    String sentTS = resultSet.getString("sent_on");
                    String deliveredTS = resultSet.getString("delivered_on");

                    Message message = new Message(pointer, content, sentTS, deliveredTS);
                    messageList.add(message);
                }
            }

        } catch (SQLException ignored) {}

        return messageList;
    }

    Integer fetchChatID(String username) throws SQLException {
        Integer chatID = null;
        selectChatIDStatement.setString(1, username);

        try(ResultSet resultSet = selectChatIDStatement.executeQuery()) {
            if (resultSet.next())
                chatID = resultSet.getInt("chat_id");

        }

        return chatID;
    }

    Chat openNewChat(String username, String name) {
        try {
            insertNewChatStatement.setString(1, username);
            insertNewChatStatement.setString(2, name);
            insertNewChatStatement.executeUpdate();

            Integer id = fetchChatID(username);
            Chat chat = new Chat(id, username, name);
            chat.messageList = new LinkedList<>();
            return chat;

        } catch (SQLException e) {
            return null;
        }
    }

    boolean insertMessage (Message message, int chatID) {
        try {
            insertMessageStatement.setInt(1, chatID);
            insertMessageStatement.setInt(2, message.pointer);
            insertMessageStatement.setString(3, message.content);

            insertMessageStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    boolean updateSTS (Message message, int chatID) {
        try {
            insertSTSStatement.setString(1, message.sentTS);
            insertSTSStatement.setInt(2, chatID);
            insertSTSStatement.setInt(3, message.pointer);

            insertSTSStatement.executeUpdate();

            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    void close() {
        try {/*
            if(insertNewChatStatement != null)
                insertNewChatStatement.close();

            if(selectChatIDStatement != null)
                selectChatIDStatement.close();

            if(statement != null)
                statement.close();*/

            if(connection != null)
                connection.close();

            isConnected = false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}