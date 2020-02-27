package com.devexperts.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel("Transfer money object")
@AllArgsConstructor
@NoArgsConstructor
public class Transfer {
    @NotNull
    @ApiModelProperty("Source account id")
    private Long sourceId;
    @NotNull
    @ApiModelProperty("Target account id")
    private Long targetId;
    @NotNull
    @Positive
    @ApiModelProperty("amount to transfer")
    private Double amount;
}
