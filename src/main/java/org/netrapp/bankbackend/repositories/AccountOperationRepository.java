package org.netrapp.bankbackend.repositories;

import org.netrapp.bankbackend.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {

    List<AccountOperation> findByBankAccountId(String bankAccount_id);

    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String bankAccount_id, Pageable pageable);
}
