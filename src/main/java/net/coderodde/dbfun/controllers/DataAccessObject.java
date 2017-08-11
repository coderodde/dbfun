package net.coderodde.dbfun.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class provides data access.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 11, 2017)
 */
public final class DataAccessObject {

    /**
     * The SQL command for inserting a person.
     */
    private static final String INSERT_PERSON_SQL = 
            "INSERT INTO funny_persons (first_name, last_name, email) VALUES " +
            "(?, ?, ?);";

    /**
     * Creates the table if not already created.
     */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS funny_persons (\n" +
                "id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,\n" +
                "first_name VARCHAR(40) NOT NULL,\n" +
                "last_name VARCHAR(40) NOT NULL,\n" +
                "email VARCHAR(50) NOT NULL,\n" +
                "created TIMESTAMP);";

    /**
     * The SQL for selecting a user given his/her ID.
     */
    private static final String GET_USER_BY_ID_SQL = 
            "SELECT * FROM funny_persons WHERE id = ?;";

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load the JDBC driver.", ex);
        }
    }
    
    private DataAccessObject() {}
    
    /**
     * Creates a connection.
     * @return
     * @throws URISyntaxException
     * @throws SQLException 
     */
    private static Connection getConnection() 
            throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("CLEARDB_DATABASE_URL"));
        
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:mysql://" + dbUri.getHost() + dbUri.getPath();
        
        return DriverManager.getConnection(dbUrl, username, password);
    }

    /**
     * Holds the data access object.
     */
    private static final DataAccessObject INSTANCE = new DataAccessObject();

    public static DataAccessObject instance() {
        return INSTANCE;
    }

    /**
     * Adds a person to the database.
     * 
     * @param person the person to add.
     */
    public void addPerson(Person person) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = 
                    connection.prepareStatement(INSERT_PERSON_SQL)) {
                statement.setString(1, person.getFirstName().trim());
                statement.setString(2, person.getLastName().trim());
                statement.setString(3, person.getEmail().trim());
                statement.executeUpdate();
            }
        } catch (SQLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates the empty database and the table.
     */
    public void createDatabase() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
//                statement.executeUpdate(CREATE_DATABASE_SQL);
//                statement.executeUpdate(USE_DATABASE_SQL);e
                statement.executeUpdate(CREATE_TABLE_SQL);
            }
        } catch (SQLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets a user by his/her ID.
     * 
     * @param id the ID of the user.
     * @return a {@code FunnyPerson} object or {@code null} if there is not such
     *         user.
     */
    public Person getUserById(int id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = 
                    connection.prepareStatement(GET_USER_BY_ID_SQL)) {
                statement.setInt(1, id);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }

                    Person person = new Person();

                    person.setId(resultSet.getInt("id"));
                    person.setFirstName(resultSet.getString("first_name"));
                    person.setLastName(resultSet.getString("last_name"));
                    person.setEmail(resultSet.getString("email"));
                    person.setCreated(resultSet.getDate("created"));

                    return person;
                }
            }
        } catch (SQLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
