package org.netrapp.bankbackend.dto;

import lombok.Data;
import org.netrapp.bankbackend.enums.AccountStatus;

import java.util.Date;


@Data
public class SavingBankAccountDTO extends BankAccountDTO{

    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDTO customerDTO;
    private double interestRate;

}
