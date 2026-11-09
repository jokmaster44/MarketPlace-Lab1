package com.cosmocats.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.mapper.ProductMapper;
import com.cosmocats.marketplace.service.ProductNotFoundException;
import com.cosmocats.marketplace.service.ProductService;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(ProductController.class)
@Import(ProductMapper.class)
class ProductControllerTest {

    private static final String VALID_PRODUCT_JSON =
            "{\"name\":\"Cosmic cat food\",\"price\":19.99}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    // Test case: A valid product request creates a product and returns its location.
    @Test
    void test_create_returnsCreatedProduct_whenRequestIsValid() throws Exception {
        when(service.create(any(Product.class))).thenReturn(product(1L));

        MockHttpServletResponse response = send(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT_JSON));

        assertEquals(201, response.getStatus(), "A valid POST should return HTTP 201");
        assertEquals("/api/products/1", response.getHeader("Location"),
                "The Location header should point to the created product");
        assertEquals(1L, ((Number) JsonPath.read(response.getContentAsString(), "$.id")).longValue(),
                "The response should contain the assigned product ID");
        assertEquals("Cosmic cat food", JsonPath.read(response.getContentAsString(), "$.name"),
                "The response should contain the product name");
    }

    // Test case: A blank product name is rejected before reaching the service.
    @Test
    void test_create_returnsBadRequest_whenNameIsBlank() throws Exception {
        MockHttpServletResponse response = send(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"   \",\"price\":19.99}"));

        assertValidationProblem(response, "name", "A blank name");
        verifyNoInteractions(service);
    }

    // Test case: A negative product price is rejected before reaching the service.
    @Test
    void test_create_returnsBadRequest_whenPriceIsNegative() throws Exception {
        MockHttpServletResponse response = send(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cosmic cat food\",\"price\":-1}"));

        assertValidationProblem(response, "price", "A negative price");
        verifyNoInteractions(service);
    }

    // Test case: A missing required price is rejected.
    @Test
    void test_create_returnsBadRequest_whenPriceIsMissing() throws Exception {
        MockHttpServletResponse response = send(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cosmic cat food\"}"));

        assertValidationProblem(response, "price", "A missing price");
        verifyNoInteractions(service);
    }

    // Test case: Malformed JSON returns a problem response.
    @Test
    void test_create_returnsBadRequest_whenJsonIsMalformed() throws Exception {
        MockHttpServletResponse response = send(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":"));

        assertEquals(400, response.getStatus(), "Malformed JSON should return HTTP 400");
        assertEquals("application/problem+json", response.getContentType(),
                "Malformed JSON should use the RFC 9457 media type");
        assertTrue(((String) JsonPath.read(response.getContentAsString(), "$.detail"))
                        .contains("valid JSON"),
                "The error should explain that the request body is invalid");
        verifyNoInteractions(service);
    }

    // Test case: A valid replacement returns the updated product.
    @Test
    void test_update_returnsUpdatedProduct_whenRequestIsValid() throws Exception {
        when(service.update(eq(1L), any(Product.class))).thenReturn(product(1L));

        MockHttpServletResponse response = send(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT_JSON));

        assertEquals(200, response.getStatus(), "A valid PUT should return HTTP 200");
        assertEquals(1L, ((Number) JsonPath.read(response.getContentAsString(), "$.id")).longValue(),
                "The updated product should keep its ID");
        assertEquals("Cosmic cat food", JsonPath.read(response.getContentAsString(), "$.name"),
                "The response should contain the updated name");
    }

    // Test case: Invalid replacement data is rejected before reaching the service.
    @Test
    void test_update_returnsBadRequest_whenNameIsBlank() throws Exception {
        MockHttpServletResponse response = send(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \",\"price\":19.99}"));

        assertValidationProblem(response, "must not be blank", "A blank name in PUT");
        verifyNoInteractions(service);
    }

    // Test case: A valid list request returns all pagination fields.
    @Test
    void test_list_returnsPage_whenPaginationIsValid() throws Exception {
        when(service.findPage(0, 20)).thenReturn(List.of(product(1L)));
        when(service.count()).thenReturn(1L);

        MockHttpServletResponse response = send(get("/api/products"));

        assertEquals(200, response.getStatus(), "A valid list request should return HTTP 200");
        assertEquals(0, ((Number) JsonPath.read(response.getContentAsString(), "$.page")).intValue(),
                "The default page should be zero");
        assertEquals(20, ((Number) JsonPath.read(response.getContentAsString(), "$.size")).intValue(),
                "The default page size should be 20");
        assertEquals(1L, ((Number) JsonPath.read(response.getContentAsString(), "$.totalElements")).longValue(),
                "The total element count should match the service");
        assertEquals(1, ((List<?>) JsonPath.read(response.getContentAsString(), "$.content")).size(),
                "The page should contain one product");
    }

    // Test case: A negative page number is rejected.
    @Test
    void test_list_returnsBadRequest_whenPageIsNegative() throws Exception {
        MockHttpServletResponse response = send(get("/api/products?page=-1"));

        assertValidationProblem(response, "page", "A negative page");
        verifyNoInteractions(service);
    }

    // Test case: A zero page size is rejected.
    @Test
    void test_list_returnsBadRequest_whenSizeIsZero() throws Exception {
        MockHttpServletResponse response = send(get("/api/products?size=0"));

        assertValidationProblem(response, "size", "A zero page size");
        verifyNoInteractions(service);
    }

    // Test case: A known product ID returns the product.
    @Test
    void test_get_returnsProduct_whenIdExists() throws Exception {
        when(service.findById(1L)).thenReturn(product(1L));

        MockHttpServletResponse response = send(get("/api/products/1"));

        assertEquals(200, response.getStatus(), "A known product ID should return HTTP 200");
        assertEquals("Cosmic cat food", JsonPath.read(response.getContentAsString(), "$.name"),
                "The response should contain the requested product");
    }

    // Test case: An unknown product ID returns a not-found problem.
    @Test
    void test_get_returnsNotFound_whenProductDoesNotExist() throws Exception {
        when(service.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        MockHttpServletResponse response = send(get("/api/products/99"));

        assertEquals(404, response.getStatus(), "An unknown product ID should return HTTP 404");
        assertEquals("application/problem+json", response.getContentType(),
                "A not-found response should use the RFC 9457 media type");
        assertTrue(((String) JsonPath.read(response.getContentAsString(), "$.detail"))
                        .contains("99"),
                "The error should identify the missing product");
    }

    // Test case: A non-positive product ID is rejected.
    @Test
    void test_get_returnsBadRequest_whenIdIsZero() throws Exception {
        MockHttpServletResponse response = send(get("/api/products/0"));

        assertValidationProblem(response, "id", "A zero product ID");
        verifyNoInteractions(service);
    }

    // Test case: Deleting a product returns no response body.
    @Test
    void test_delete_returnsNoContent_whenIdExists() throws Exception {
        MockHttpServletResponse response = send(delete("/api/products/1"));

        assertEquals(204, response.getStatus(), "A successful DELETE should return HTTP 204");
        assertTrue(response.getContentAsString().isEmpty(),
                "A successful DELETE should not return a response body");
    }

    private MockHttpServletResponse send(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse();
    }

    private static Product product(long id) {
        return new Product(id, "Cosmic cat food", new BigDecimal("19.99"));
    }

    private static void assertValidationProblem(
            MockHttpServletResponse response, String expectedDetail, String scenario) throws Exception {
        assertEquals(400, response.getStatus(), scenario + " should return HTTP 400");
        assertEquals("application/problem+json", response.getContentType(),
                scenario + " should use the RFC 9457 media type");
        assertTrue(((String) JsonPath.read(response.getContentAsString(), "$.detail"))
                        .contains(expectedDetail),
                scenario + " should explain the validation problem: " + expectedDetail);
    }
}
