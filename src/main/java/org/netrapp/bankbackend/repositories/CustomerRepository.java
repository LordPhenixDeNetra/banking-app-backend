package org.netrapp.bankbackend.repositories;

import org.netrapp.bankbackend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

//    List<Customer> findByNameContains(String keyword);
    @Query("SELECT c FROM Customer c where c.name like :kw")
    List<Customer> searchCustomer(@Param("kw") String keyword);
}
