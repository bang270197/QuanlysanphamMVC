package com.codegym.service;

import com.codegym.model.Product;

import java.util.ArrayList;
import java.util.List;

public interface ProductService {

    List<Product> findAll();
    void save (Product product);
    void edit (int id,Product product);
    void delete (int id);
    Product findById(int id);
    ArrayList<Product> findByName(String name);

}
