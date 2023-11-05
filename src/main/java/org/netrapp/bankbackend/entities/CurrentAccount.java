package org.netrapp.bankbackend.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("CA")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAccount extends BankAccount{

    private double overDraft;
}
