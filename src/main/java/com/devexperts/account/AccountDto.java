package com.devexperts.account;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class AccountDto {

    @NotNull
    @ApiModelProperty("account id")
    private long id;
    @ApiModelProperty("account holder first name")
    private String firstName;
    @ApiModelProperty("account holder last name")
    private String lastName;
    @NotNull
    @ApiModelProperty("account balance, usd")
    private Double balance;
}
