package de.minikonto.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

/** API tests: call the REST endpoints like a real client and check status codes and JSON. */
@SpringBootTest
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private long openAccount(String owner, String deposit) throws Exception {
        String body = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"owner\":\"" + owner + "\",\"initialDeposit\":" + deposit + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String transferJson(long from, long to, String amount) {
        return "{\"fromAccountId\":" + from + ",\"toAccountId\":" + to + ",\"amount\":" + amount + "}";
    }

    @Test
    @DisplayName("POST /api/accounts creates an account, GET returns it")
    void createAndGet() throws Exception {
        long id = openAccount("Anna", "100");
        mvc.perform(get("/api/accounts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value("Anna"))
                .andExpect(jsonPath("$.balance").value("100.00"));
    }

    @Test
    @DisplayName("POST /api/transfers moves money (201)")
    void transfer() throws Exception {
        long anna = openAccount("Anna", "100");
        long ben = openAccount("Ben", "0");

        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(anna, ben, "25.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value("25.50"));

        mvc.perform(get("/api/accounts/" + anna)).andExpect(jsonPath("$.balance").value("74.50"));
        mvc.perform(get("/api/accounts/" + ben)).andExpect(jsonPath("$.balance").value("25.50"));
        mvc.perform(get("/api/accounts/" + ben + "/transfers")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Insufficient funds returns 422 and changes nothing")
    void insufficientFunds() throws Exception {
        long anna = openAccount("Anna", "10");
        long ben = openAccount("Ben", "0");

        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(anna, ben, "50")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Insufficient funds")));

        mvc.perform(get("/api/accounts/" + anna)).andExpect(jsonPath("$.balance").value("10.00"));
    }

    @Test
    @DisplayName("GET /api/accounts lists all accounts")
    void listAccounts() throws Exception {
        long id = openAccount("Dora", "5");
        mvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].owner").value("Dora"));
    }

    @Test
    @DisplayName("Web interface is served")
    void webInterface() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("Mini-Konto")));
    }

    @Test
    @DisplayName("Unknown account returns 404")
    void unknownAccount() throws Exception {
        mvc.perform(get("/api/accounts/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Negative amount returns 400")
    void negativeAmount() throws Exception {
        long anna = openAccount("Anna", "10");
        mvc.perform(post("/api/accounts/" + anna + "/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing owner returns 400")
    void missingOwner() throws Exception {
        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"owner\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
