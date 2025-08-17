package com.mycompany.payrollsystem.models;

import javafx.beans.property.*;

/**
 * model for employee records.
 * wraps employee demographics and contact info in JavaFX properties
 * so they can be bound directly to UI tables and forms.
 */
public class Employee {
    //unique employee id, e.g. E001
    private final StringProperty employeeid;
    //first name
    private final StringProperty firstName;
    //last name
    private final StringProperty lastName;
    //middle name (nullable)
    private final StringProperty middleName;
    //date of birth yyyy-mm-dd
    private final StringProperty dob;
    //phone number
    private final StringProperty phone;
    //company email
    private final StringProperty email;
    //employment status (active or terminated)
    private final StringProperty status;
    //gender (male or female)
    private final StringProperty gender;
    //pay type (salary or hourly)
    private final StringProperty payType;
    //street address line 1
    private final StringProperty addressLine1;
    //street address line 2 (optional)
    private final StringProperty addressLine2;
    //city
    private final StringProperty city;
    //state abbreviation
    private final StringProperty state;
    //zip code
    private final StringProperty zip;

    /**
     * constructs an employee with all details from database row.
     *
     * @param employeeid unique employee id
     * @param firstName given name
     * @param lastName family name
     * @param middleName optional middle name
     * @param dob date of birth yyyy-mm-dd
     * @param phone phone number
     * @param email company email
     * @param status employment status (active or terminated)
     * @param gender gender string
     * @param payType pay type (salary or hourly)
     * @param addressLine1 primary street address
     * @param addressLine2 secondary street address
     * @param city city
     * @param state state abbreviation
     * @param zip postal code
     */
    public Employee(String employeeid, String firstName, String lastName, String middleName, String dob, String phone,
                    String email, String status, String gender, String payType,
                    String addressLine1, String addressLine2, String city, String state, String zip) {

        this.employeeid = new SimpleStringProperty(employeeid);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.middleName = new SimpleStringProperty(middleName);
        this.dob = new SimpleStringProperty(dob);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
        this.status = new SimpleStringProperty(status);
        this.gender = new SimpleStringProperty(gender);
        this.payType = new SimpleStringProperty(payType);
        this.addressLine1 = new SimpleStringProperty(addressLine1);
        this.addressLine2 = new SimpleStringProperty(addressLine2);
        this.city = new SimpleStringProperty(city);
        this.state = new SimpleStringProperty(state);
        this.zip = new SimpleStringProperty(zip);
    }

    //getter, setter, and property accessors for ui binding
    public String getEmployeeId() { return employeeid.get(); }
    public void setId(String value) { employeeid.set(value); }
    public StringProperty idProperty() { return employeeid; }

    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String value) { firstName.set(value); }
    public StringProperty firstNameProperty() { return firstName; }

    public String getLastName() { return lastName.get(); }
    public void setLastName(String value) { lastName.set(value); }
    public StringProperty lastNameProperty() { return lastName; }

    public String getMiddleName() { return middleName.get(); }
    public void setMiddleName(String value) { middleName.set(value); }
    public StringProperty middleNameProperty() { return middleName; }

    public String getDob() { return dob.get(); }
    public void setDob(String value) { dob.set(value); }
    public StringProperty dobProperty() { return dob; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getGender() { return gender.get(); }
    public void setGender(String value) { gender.set(value); }
    public StringProperty genderProperty() { return gender; }

    public String getPayType() { return payType.get(); }
    public void setPayType(String value) { payType.set(value); }
    public StringProperty payTypeProperty() { return payType; }

    public String getAddressLine1() { return addressLine1.get(); }
    public void setAddressLine1(String value) { addressLine1.set(value); }
    public StringProperty addressLine1Property() { return addressLine1; }

    public String getAddressLine2() { return addressLine2.get(); }
    public void setAddressLine2(String value) { addressLine2.set(value); }
    public StringProperty addressLine2Property() { return addressLine2; }

    public String getCity() { return city.get(); }
    public void setCity(String value) { city.set(value); }
    public StringProperty cityProperty() { return city; }

    public String getState() { return state.get(); }
    public void setState(String value) { state.set(value); }
    public StringProperty stateProperty() { return state; }

    public String getZip() { return zip.get(); }
    public void setZip(String value) { zip.set(value); }
    public StringProperty zipProperty() { return zip; }
}
