package com.kimani.springsecuritykeycloak.repositories;

import com.kimani.springsecuritykeycloak.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Since 07/01/2023
 * @Author: Kimani Kelvin
 * @Contact: kelvinkimaniapps@gmail.com
 */


@Repository
public interface CustomersRepository extends JpaRepository<Customer, Integer> {
}
