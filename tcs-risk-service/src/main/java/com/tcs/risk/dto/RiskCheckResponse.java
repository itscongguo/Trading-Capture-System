package com.tcs.risk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskCheckResponse {
    private boolean approved;
    private String reason;
    private String riskDecisionId;
}
