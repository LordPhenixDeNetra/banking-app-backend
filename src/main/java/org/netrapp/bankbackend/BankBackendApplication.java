package org.netrapp.bankbackend;

import org.netrapp.bankbackend.dto.BankAccountDTO;
import org.netrapp.bankbackend.dto.CurrentBankAccountDTO;
import org.netrapp.bankbackend.dto.CustomerDTO;
import org.netrapp.bankbackend.dto.SavingBankAccountDTO;
import org.netrapp.bankbackend.entities.*;
import org.netrapp.bankbackend.enums.AccountStatus;
import org.netrapp.bankbackend.enums.OperationType;
import org.netrapp.bankbackend.exceptions.BalanceNotSufficientException;
import org.netrapp.bankbackend.exceptions.BankAccountNotFoundException;
import org.netrapp.bankbackend.exceptions.CustomerNotFoundException;
import org.netrapp.bankbackend.repositories.AccountOperationRepository;
import org.netrapp.bankbackend.repositories.BankAccountRepository;
import org.netrapp.bankbackend.repositories.CustomerRepository;
import org.netrapp.bankbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class BankBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService){

        return  args -> {
            Stream.of("Thior", "Netra", "Nhkr").forEach(name->{
                CustomerDTO customerDTO = new CustomerDTO();
                customerDTO.setName(name);
                customerDTO.setEmail(name.toLowerCase()+"@gmail.com");

                bankAccountService.saveCustomer(customerDTO);
            });

            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentAccount(Math.random()*90000, 9000, customer.getId());
                    bankAccountService.saveSavingAccount(Math.random()*120000, 5.5, customer.getId());

                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });
            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for (BankAccountDTO bankAccount : bankAccounts){
                for (int i = 0; i < Math.random()*10; i++) {
                    String accountId;

                    if (bankAccount instanceof SavingBankAccountDTO){
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    }else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000+Math.random()*120000, "Credit");
                    bankAccountService.debit(accountId, 1000+Math.random()*9000, "Debit");
                }
            }


        };

    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository){
        return args -> {
            Stream.of("Moussa", "Lord", "Phenix").forEach(name->{
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name.toLowerCase()+"@gmail.com");

                customerRepository.save(customer);
            });

            customerRepository.findAll().forEach(customer -> {

                CurrentAccount currentAccount = new CurrentAccount();
                SavingAccount savingAccount = new SavingAccount();

                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random()*90000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(customer);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);

                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random()*90000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(customer);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });

            bankAccountRepository.findAll().forEach(bankAccount -> {

                for (int i = 0; i < Math.random() * 10; i++) {
                    AccountOperation accountOperation = new AccountOperation();

                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
                    accountOperation.setBankAccount(bankAccount);

                    accountOperationRepository.save(accountOperation);
                }

            });
        };
    }

}
