package com.kimani.springsecuritykeycloak.controllers;

import com.kimani.springsecuritykeycloak.dto.CustomerDto;
import com.kimani.springsecuritykeycloak.services.CustomersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Since 07/01/2023
 * @Author: Kimani Kelvin
 * @Contact: kelvinkimaniapps@gmail.com
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomersController {

    private final CustomersService customersService;

    @PostMapping
    public ResponseEntity<HttpStatus> addNewCustomer(@RequestBody CustomerDto dto) {
        customersService.createCustomer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        return customersService.getAllCustomers();
    }

    @GetMapping("{id}")
    public CustomerDto getCustomerById(@PathVariable Integer id) {
        return customersService.getCustomerDtoById(id);
    }

    @PutMapping("{id}")
    public CustomerDto updateCustomerById(
            @PathVariable Integer id,
            @RequestBody CustomerDto dto) {
        return customersService.updateCustomerById(id, dto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<HttpStatus> deleteCustomerById(@PathVariable Integer id) {
        customersService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
