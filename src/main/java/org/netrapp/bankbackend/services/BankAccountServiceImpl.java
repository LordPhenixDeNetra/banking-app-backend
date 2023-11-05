package org.netrapp.bankbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.netrapp.bankbackend.dto.*;
import org.netrapp.bankbackend.entities.*;
import org.netrapp.bankbackend.enums.OperationType;
import org.netrapp.bankbackend.exceptions.BalanceNotSufficientException;
import org.netrapp.bankbackend.exceptions.BankAccountNotFoundException;
import org.netrapp.bankbackend.exceptions.CustomerNotFoundException;
import org.netrapp.bankbackend.mappers.BankAccountMapperImpl;
import org.netrapp.bankbackend.repositories.AccountOperationRepository;
import org.netrapp.bankbackend.repositories.BankAccountRepository;
import org.netrapp.bankbackend.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService{

    private final CustomerRepository customerRepository;
    private  final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapperImpl dtoMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving Customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {

        Customer customer = customerRepository.findById(customerId).orElse(null);

        if (customer == null){
            throw new CustomerNotFoundException("Customer not found");
        }

        CurrentAccount currentAccount = new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedCurrentAccount = bankAccountRepository.save(currentAccount);

        return dtoMapper.fromCurrentBankAccount(savedCurrentAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {

        Customer customer = customerRepository.findById(customerId).orElse(null);

        if (customer == null){
            throw new CustomerNotFoundException("Customer not found");
        }

        SavingAccount savingAccount = new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);

        SavingAccount savedSavingAccount = bankAccountRepository.save(savingAccount);

        return dtoMapper.fromSavingBankAccount(savedSavingAccount);
    }


    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        /*
        List<CustomerDTO> customerDTOs = new ArrayList<>();
        for (Customer customer : customers){
            CustomerDTO customerDTO = dtoMapper.fromCustomer(customer);
            customerDTOs.add(customerDTO);
        }
        */

        List<CustomerDTO> customerDTOs = customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());

        return customerDTOs;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank Account Not Found"));
        if (bankAccount instanceof SavingAccount){
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        }else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }

    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank Account Not Found"));

        if (bankAccount.getBalance() < amount){
            throw new BalanceNotSufficientException("Balance Not Sufficient");
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException{

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()-> new BankAccountNotFoundException("Bank Account Not Found"));

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);

    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOs = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);

            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());

        return bankAccountDTOs;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new CustomerNotFoundException("Customer not found"));

        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving Customer");
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);

        return accountOperations.stream()
                .map(op->dtoMapper.fromAccountOperation(op))
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount == null){
            throw new BankAccountNotFoundException("Bank Account not found");
        }
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();

        List<AccountOperationDTO> accountOperationDTOs = accountOperations.getContent().stream().map(accountOperation -> dtoMapper.fromAccountOperation(accountOperation)).collect(Collectors.toList());

        accountHistoryDTO.setAccountOperationDTOs(accountOperationDTOs);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setSizePage(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());

        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers = customerRepository.searchCustomer(keyword);
        List<CustomerDTO> customerDTOs = customers.stream().map(customer -> dtoMapper.fromCustomer(customer)).collect(Collectors.toList());
        return customerDTOs;
    }


}
