package net.coderodde.dbfun.controllers;

import java.sql.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents persons. It makes sure that first and last names are
 * not {@code null} or empty. Also, it validates the email addresses. Once an
 * instance of this class constructed, the client programmer may rest assured
 * that it is possible to put into the database.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 11, 2017)
 */
public final class Person {
    
    /**
     * Used for validating the email addresses.
     */
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = 
                        Pattern.compile(
                                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                                Pattern.CASE_INSENSITIVE);

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private Date created;

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Date getCreated() {
        return created;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        Objects.requireNonNull(firstName, "First name is null.");
        
        if (firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is null");
        }
        
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        Objects.requireNonNull(lastName, "Last name is null.");
        
        if (lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is null");
        }
        
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        Objects.requireNonNull(email, "Email address is null.");
        
        if (!validate(email)) {
            throw new IllegalArgumentException();
        }
        
        this.email = email;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Override
    public String toString() {
        return "{id=" + id + ", firstName=\"" + firstName + "\", lastName=\"" +
                lastName + "\", email=\"" + email + "\", created=\"" + created +
                "\"}"; 
    }
    
    /**
     * Checks the email address.
     * 
     * @param email the email address to validate.
     * @return {@code true} if {@code email} is a valid email address.
     */
    private static boolean validate(String email ) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email );
        return matcher.find();
    }
}
