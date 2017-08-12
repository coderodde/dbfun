package net.coderodde.dbfun.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides data access.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 11, 2017)
 */
public final class DataAccessObject {

    /**
     * The name of the column holding the ID.
     */
    private static final String ID_COLUMN = "id";
    
    /**
     * The name of the column holding the first name.
     */
    private static final String FIRST_NAME_COLUMN = "first_name";
    
    /**
     * The name of the column holding the last name.
     */
    private static final String LAST_NAME_COLUMN = "last_name";
    
    /**
     * The name of the column holding the email address.
     */
    private static final String EMAIL_COLUMN = "email";
    
    /**
     * The name of the column holding the creating time stamp.
     */
    private static final String CREATED_COLUMN = "created";
    
    /**
     * The name of the table holding the users.
     */
    private static final String PERSON_TABLE_NAME = "funny_persons";
    
    /**
     * The SQL command for inserting a person.
     */
    private static final String INSERT_PERSON_SQL = 
            "INSERT INTO " + PERSON_TABLE_NAME + " (" +
            FIRST_NAME_COLUMN + ", " +
            LAST_NAME_COLUMN + ", " + 
            EMAIL_COLUMN + ") VALUES (?, ?, ?);";

    /**
     * Creates the table if not already created.
     */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + PERSON_TABLE_NAME + " (\n" +
                ID_COLUMN         + " INT(6) UNSIGNED " + 
                                    "AUTO_INCREMENT PRIMARY KEY,\n" +
                FIRST_NAME_COLUMN + " VARCHAR(40) NOT NULL,\n" +
                LAST_NAME_COLUMN  + " VARCHAR(40) NOT NULL,\n" +
                EMAIL_COLUMN      + " VARCHAR(50) NOT NULL,\n" +
                CREATED_COLUMN    + " TIMESTAMP);";

    /**
     * The SQL command for selecting a user given his/her ID.
     */
    private static final String GET_USER_BY_ID_SQL = 
            "SELECT * FROM " + PERSON_TABLE_NAME + " WHERE " +
            ID_COLUMN + " = ?;";

    /**
     * The SQL command for selecting all users.
     */
    private static final String GET_ALL_USERS = 
            "SELECT * FROM " + PERSON_TABLE_NAME + ";";
    
    /**
     * The SQL command for getting the number of users.
     */
    private static final String GET_NUMBER_OF_USERS = 
            "SELECT COUNT(*) FROM " + PERSON_TABLE_NAME + ";";
    
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
                statement.executeUpdate(CREATE_TABLE_SQL);
            }
        } catch (SQLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This method returns the list of all users in the database.
     * 
     * @return the list of all persons.
     */
    public List<Person> getAllUsers() {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(GET_ALL_USERS)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Person> persons = new ArrayList<>(getNumberOfUsers());
                    
                    while (resultSet.next()) {
                        Person person = new Person();
                        
                        person.setId(resultSet.getInt(ID_COLUMN));
                        person.setFirstName(
                                resultSet.getString(FIRST_NAME_COLUMN));
                        
                        person.setLastName(
                                resultSet.getString(LAST_NAME_COLUMN));
                        
                        person.setEmail(resultSet.getString(EMAIL_COLUMN));
                        person.setCreated(resultSet.getDate(CREATED_COLUMN));
                        
                        persons.add(person);
                    }
                    
                    return persons;
                }
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
    
    /**
     * Gets the number of users in the database.
     * 
     * @return the number of users.
     */
    private int getNumberOfUsers() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = 
                        statement.executeQuery(GET_NUMBER_OF_USERS)) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        } 
    }
}
