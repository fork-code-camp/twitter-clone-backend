package com.example.authentication.controller.dto;

import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
public class RegisterRequest {

    String email;
    String password;
}
