package com.devexperts.rest;

import static com.devexperts.util.AccountUtil.FIRST_NAME;
import static com.devexperts.util.AccountUtil.LAST_NAME;
import static com.devexperts.util.AccountUtil.account;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devexperts.account.Account;
import com.devexperts.account.AccountDto;
import com.devexperts.account.Transfer;
import com.devexperts.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountService accountService;

    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        accountService.clear();
    }

    @Test
    void createAccount() throws Exception {

        long id = 1L;

        AccountDto account = new AccountDto().setId(id).setBalance(1.);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isOk());

        Account created = accountService.getAccount(id);
        assertNotNull(created);
        assertEquals(id, created.getAccountId());
    }

    @Test
    void getAccount() throws Exception {

        long id = 2L;
        accountService.createAccount(account(id, 3.));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/account/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(2)))
                .andExpect(jsonPath("$.balance", equalTo(3.0)))
                .andExpect(jsonPath("$.firstName", equalTo(FIRST_NAME)))
                .andExpect(jsonPath("$.lastName", equalTo(LAST_NAME)));
    }

    @Test
    void clearAccounts() throws Exception {
        long id = 3L;
        accountService.createAccount(account(id, 3.));
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/account"))
                .andExpect(status().isOk());

        assertNull(accountService.getAccount(id));
    }

    @Test
    void transfer() throws Exception {

        long sourceId = 4L;
        accountService.createAccount(account(sourceId, 3.));

        long targetId = 5L;
        accountService.createAccount(account(targetId, 3.));

        Transfer t = new Transfer(sourceId, targetId, 1.);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isOk());

        Account source = accountService.getAccount(sourceId);
        assertNotNull(source);
        assertEquals(2., source.getBalance());

        Account target = accountService.getAccount(targetId);
        assertNotNull(target);
        assertEquals(4., target.getBalance());
    }

    @Test
    void transferException() throws Exception {

        long sourceId = 4L;
        accountService.createAccount(account(sourceId, 3.));

        long targetId = 5L;
        accountService.createAccount(account(targetId, 3.));

        Transfer t = new Transfer(sourceId, 9L, 1.);

        // target not found
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().is4xxClientError())
                .andExpect(status().is(404));

        // insufficient funds
        t = new Transfer(sourceId, targetId, 5.);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().is5xxServerError())
                .andExpect(status().is(500));

        // wrong amount
        t = new Transfer(sourceId, targetId, -1.);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().is4xxClientError())
                .andExpect(status().is(400));

    }
}