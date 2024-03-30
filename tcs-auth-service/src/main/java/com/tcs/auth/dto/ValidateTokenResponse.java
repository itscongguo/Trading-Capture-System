package com.tcs.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

    private boolean valid;
    private String userId;
    private String username;
    private String accountId;
    private String role;
}
