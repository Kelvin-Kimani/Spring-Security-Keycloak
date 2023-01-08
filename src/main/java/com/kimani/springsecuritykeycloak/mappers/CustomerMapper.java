package com.kimani.springsecuritykeycloak.mappers;

import com.kimani.springsecuritykeycloak.dto.CustomerDto;
import com.kimani.springsecuritykeycloak.models.Customer;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @Since 07/01/2023
 * @Author: Kimani Kelvin
 * @Contact: kelvinkimaniapps@gmail.com
 */


@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer customerDtoToCustomer(CustomerDto dto);

    CustomerDto customerToCustomerDto(Customer customer);

    List<CustomerDto> customersToCustomerDtos(List<Customer> customers);
}
