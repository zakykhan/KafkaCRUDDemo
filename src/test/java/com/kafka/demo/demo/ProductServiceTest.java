package com.kafka.demo.demo;

import com.kafka.demo.demo.service.ProductService;
import com.kafka.demo.demo.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import com.kafka.demo.demo.repository.ProductRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
    }

    // Test case for successful product creation
    @Test
    public void testCreateProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product createdProduct = productService.createProduct(product);

        assertNotNull(createdProduct);
        assertEquals(1L, createdProduct.getId());
        verify(kafkaTemplate, times(1)).send(eq("product-events"), contains("Product Created"));
    }

    // Test case for product creation failure
    @Test
    public void testCreateProduct_Failure() {
        when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(product);
        });

        assertEquals("Database error", exception.getMessage());
        verify(kafkaTemplate, never()).send(eq("product-events"), anyString());
    }

    // Test case for updating a product successfully
    @Test
    public void testUpdateProduct_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product updatedProduct = productService.updateProduct(1L, product);

        assertNotNull(updatedProduct);
        assertEquals("Test Product", updatedProduct.getName());
        verify(kafkaTemplate, times(1)).send(eq("product-events"), contains("Product Updated"));
    }

    // Test case for product update failure when product does not exist
    @Test
    public void testUpdateProduct_Failure() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(1L, product);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(kafkaTemplate, never()).send(eq("product-events"), anyString());
    }

    // Test case for deleting a product successfully
    @Test
    public void testDeleteProduct_Success() {
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteProduct(1L);

        verify(kafkaTemplate, times(1)).send(eq("product-events"), contains("Product Deleted"));
        verify(productRepository, times(1)).deleteById(1L);
    }

    // Test case for delete product failure
    @Test
    public void testDeleteProduct_Failure() {
        doThrow(new RuntimeException("Deletion error")).when(productRepository).deleteById(anyLong());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(1L);
        });

        assertEquals("Deletion error", exception.getMessage());
        verify(kafkaTemplate, never()).send(eq("product-events"), anyString());
    }
}

