package com.cosmocats.marketplace.service;


public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(long id){
        super("Product with id " + id + " was not found");
    }
}
