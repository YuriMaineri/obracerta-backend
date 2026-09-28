package br.com.obracerta.company;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/company/bank-accounts")
class BankAccountController {

    private final CompanyService service;

    BankAccountController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    List<BankAccountResponse> list() {
        return service.listBankAccounts().stream().map(BankAccountResponse::from).toList();
    }

    @PostMapping
    ResponseEntity<BankAccountResponse> create(@RequestBody @Valid BankAccountRequest request,
                                               UriComponentsBuilder uriBuilder) {
        BankAccount account = service.createBankAccount(request);
        var location = uriBuilder.path("/api/company/bank-accounts/{id}").buildAndExpand(account.getId()).toUri();
        return ResponseEntity.created(location).body(BankAccountResponse.from(account));
    }

    @PutMapping("/{id}")
    BankAccountResponse update(@PathVariable Long id, @RequestBody @Valid BankAccountRequest request) {
        return BankAccountResponse.from(service.updateBankAccount(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivateBankAccount(id);
        return ResponseEntity.noContent().build();
    }

    record BankAccountResponse(Long id, String bank, String branch, String accountNumber, String accountType,
                               String pixKey, String holder, boolean defaultAccount) {

        static BankAccountResponse from(BankAccount a) {
            return new BankAccountResponse(a.getId(), a.getBank(), a.getBranch(), a.getAccountNumber(),
                    a.getAccountType(), a.getPixKey(), a.getHolder(), a.isDefaultAccount());
        }
    }
}
