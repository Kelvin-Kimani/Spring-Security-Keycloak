package com.kimani.springsecuritykeycloak.services;

import com.kimani.springsecuritykeycloak.dto.CustomerDto;
import com.kimani.springsecuritykeycloak.exceptions.ResourceNotFoundException;
import com.kimani.springsecuritykeycloak.mappers.CustomerMapper;
import com.kimani.springsecuritykeycloak.models.Customer;
import com.kimani.springsecuritykeycloak.repositories.CustomersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Since 07/01/2023
 * @Author: Kimani Kelvin
 * @Contact: kelvinkimaniapps@gmail.com
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersService {
    private final CustomersRepository customersRepository;
    private final CustomerMapper customerMapper;

    public void createCustomer(CustomerDto dto) {
        var customer = customerMapper.customerDtoToCustomer(dto);
        customersRepository.save(customer);
    }

    public List<CustomerDto> getAllCustomers() {
        return customersRepository.findAll().stream().map(customerMapper::customerToCustomerDto).toList();
    }

    public CustomerDto getCustomerDtoById(Integer id) {
        var customer = getCustomerById(id);
        return customerMapper.customerToCustomerDto(customer);
    }

    public CustomerDto updateCustomerById(Integer id, CustomerDto dto) {
        var customer = getCustomerById(id);
        customer = customerMapper.customerDtoToCustomer(dto);
        Customer saved = customersRepository.save(customer);
        return customerMapper.customerToCustomerDto(saved);
    }


    public Customer getCustomerById(Integer id) {
        return customersRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Customer with id %d not found", id)));
    }

    public void deleteById(Integer id) {
        customersRepository.deleteById(id);
    }
}
