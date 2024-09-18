package com.kafka.demo.demo.repository;


import com.kafka.demo.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}

