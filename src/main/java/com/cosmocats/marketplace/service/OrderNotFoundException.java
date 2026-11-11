package com.cosmocats.marketplace.service;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order with id " + id + " was not found");
    }
}
