package br.com.obracerta.company;

import jakarta.persistence.*;

/** Conta para depósito e PIX. Uma delas é a padrão, sugerida em todo orçamento novo. */
@Entity
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String bank;

    @Column(length = 20)
    private String branch;

    @Column(length = 30)
    private String accountNumber;

    @Column(length = 30)
    private String accountType;

    @Column(length = 150)
    private String pixKey;

    @Column(length = 200)
    private String holder;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAccount;

    @Column(nullable = false)
    private boolean active = true;

    protected BankAccount() {
    }

    BankAccount(Long companyId) {
        this.companyId = companyId;
    }

    void update(BankAccountRequest request) {
        this.bank = request.bank().trim();
        this.branch = blankToNull(request.branch());
        this.accountNumber = blankToNull(request.accountNumber());
        this.accountType = blankToNull(request.accountType());
        this.pixKey = blankToNull(request.pixKey());
        this.holder = blankToNull(request.holder());
    }

    void markAsDefault() {
        this.defaultAccount = true;
    }

    void unmarkDefault() {
        this.defaultAccount = false;
    }

    void deactivate() {
        this.active = false;
        this.defaultAccount = false;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getBank() { return bank; }
    public String getBranch() { return branch; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public String getPixKey() { return pixKey; }
    public String getHolder() { return holder; }
    public boolean isDefaultAccount() { return defaultAccount; }
    public boolean isActive() { return active; }
}
