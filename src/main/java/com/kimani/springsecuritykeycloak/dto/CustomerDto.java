package com.kimani.springsecuritykeycloak.dto;

/**
 * @Since 07/01/2023
 * @Author: Kimani Kelvin
 * @Contact: kelvinkimaniapps@gmail.com
 */

public record CustomerDto(Integer id, String name, String email, Integer age, Integer idNumber, String phoneNumber) {
}
