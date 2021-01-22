package com.cheeseind.blogengine.models.dto.authdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestorePasswordRequest {

    @Email
    private String email;
}
