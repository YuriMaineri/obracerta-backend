package br.com.obracerta.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    CustomerService service;

    @Test
    void createReturns201WithLocation() {
        given(service.create(any())).willReturn(
                new Customer(Customer.PersonType.COMPANY, "Cartorio Registro Civil 2a Zona", null));

        var response = mvc.post().uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"personType":"COMPANY","name":"Cartorio Registro Civil 2a Zona"}
                        """)
                .exchange();

        assertThat(response).hasStatus(201).containsHeader("Location");
        assertThat(response).bodyJson().extractingPath("$.name").isEqualTo("Cartorio Registro Civil 2a Zona");
    }

    @Test
    void blankNameReturns400() {
        var response = mvc.post().uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"personType":"COMPANY","name":""}
                        """)
                .exchange();

        assertThat(response).hasStatus(400);
    }

    @Test
    void invalidEmailReturns400WithFieldMessage() {
        var response = mvc.post().uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"personType":"COMPANY","name":"Cartorio","email":"joao@empresa"}
                        """)
                .exchange();

        assertThat(response).hasStatus(400);
        assertThat(response).bodyJson().extractingPath("$.detail").isEqualTo("E-mail invalido");
    }

    @Test
    void unknownCustomerReturns404() {
        given(service.findById(99L)).willThrow(new NoSuchElementException("Cliente nao encontrado: 99"));

        assertThat(mvc.get().uri("/api/customers/99")).hasStatus(404);
    }
}
