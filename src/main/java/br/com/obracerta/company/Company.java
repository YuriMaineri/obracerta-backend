package br.com.obracerta.company;

import jakarta.persistence.*;
import java.time.Instant;

/** Dados da própria empresa, usados no cabeçalho e no rodapé da proposta. */
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String legalName;

    @Column(length = 200)
    private String tradeName;

    @Column(length = 20)
    private String taxId;

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

    /** Nome que assina a proposta ("Carlos Maineri"). */
    @Column(length = 150)
    private String signatoryName;

    private byte[] logo;

    @Column(length = 50)
    private String logoContentType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Company() {
    }

    void update(CompanyRequest request, String taxId, String phone, String postalCode) {
        this.legalName = request.legalName().trim();
        this.tradeName = blankToNull(request.tradeName());
        this.taxId = taxId;
        this.phone = phone;
        this.email = request.email() == null || request.email().isBlank() ? null : request.email().trim().toLowerCase();
        this.address = blankToNull(request.address());
        this.district = blankToNull(request.district());
        this.city = blankToNull(request.city());
        this.postalCode = postalCode;
        this.signatoryName = blankToNull(request.signatoryName());
    }

    void replaceLogo(byte[] content, String contentType) {
        this.logo = content;
        this.logoContentType = contentType;
    }

    void removeLogo() {
        this.logo = null;
        this.logoContentType = null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public String getLegalName() { return legalName; }
    public String getTradeName() { return tradeName; }
    public String getTaxId() { return taxId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getSignatoryName() { return signatoryName; }
    public byte[] getLogo() { return logo; }
    public String getLogoContentType() { return logoContentType; }
    public boolean hasLogo() { return logo != null && logo.length > 0; }
}
