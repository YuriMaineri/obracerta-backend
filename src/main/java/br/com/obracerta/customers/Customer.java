package br.com.obracerta.customers;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** Cliente da empresa, pessoa física ou jurídica. */
@Entity
@Table(name = "customer")
public class Customer {

    public enum PersonType { INDIVIDUAL, COMPANY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PersonType personType = PersonType.COMPANY;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String taxId;

    @Column(length = 150)
    private String contactPerson;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 250)
    private String address;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String postalCode;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Customer() {
    }

    public Customer(PersonType personType, String name, String taxId) {
        this.personType = personType;
        this.name = name;
        this.taxId = taxId;
    }

    public void updateIdentification(PersonType personType, String name, String taxId) {
        this.personType = personType;
        this.name = name;
        this.taxId = taxId;
    }

    public void updateContact(String contactPerson, String phone, String email) {
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
    }

    public void updateAddress(String address, String district, String city, String postalCode) {
        this.address = address;
        this.district = district;
        this.city = city;
        this.postalCode = postalCode;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() { return id; }
    public PersonType getPersonType() { return personType; }
    public String getName() { return name; }
    public String getTaxId() { return taxId; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
