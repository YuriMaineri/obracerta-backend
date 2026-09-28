package br.com.obracerta.company.internal;

import br.com.obracerta.company.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByCompanyIdAndActiveTrueOrderByDefaultAccountDescBankAsc(Long companyId);

    Optional<BankAccount> findByIdAndCompanyIdAndActiveTrue(Long id, Long companyId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update BankAccount b set b.defaultAccount = false where b.companyId = :companyId and b.id <> :keepId")
    void clearDefaultExcept(@Param("companyId") Long companyId, @Param("keepId") Long keepId);
}
