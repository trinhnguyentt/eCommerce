package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findByStreet(String street);
}
