package com.cosmocats.marketplace.service;

import com.cosmocats.marketplace.domain.Order;
import com.cosmocats.marketplace.domain.Product;
import com.cosmocats.marketplace.persistence.entity.OrderEntity;
import com.cosmocats.marketplace.persistence.entity.OrderItemEntity;
import com.cosmocats.marketplace.persistence.entity.ProductEntity;
import com.cosmocats.marketplace.persistence.repository.OrderRepository;
import com.cosmocats.marketplace.persistence.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public Order create(Order order) {
        OrderEntity entity = new OrderEntity(BigDecimal.ZERO);
        replaceItems(entity, order.products());
        return toDomain(orders.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Order findById(long id) {
        return toDomain(orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orders.findAll().stream().map(this::toDomain).toList();
    }

    public Order update(long id, Order replacement) {
        OrderEntity entity = orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        replaceItems(entity, replacement.products());
        return toDomain(orders.saveAndFlush(entity));
    }

    public void delete(long id) {
        OrderEntity entity = orders.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        orders.delete(entity);
    }

    private void replaceItems(OrderEntity order, List<Product> requestedProducts) {
        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Product requested : requestedProducts) {
            ProductEntity product = products.findById(requested.id())
                    .orElseThrow(() -> new ProductNotFoundException(requested.id()));
            items.add(new OrderItemEntity(product, 1, product.getPrice()));
            total = total.add(product.getPrice());
        }
        order.replaceItems(items, total);
    }

    private Order toDomain(OrderEntity entity) {
        List<Product> items = entity.getItems().stream().map(item -> {
            ProductEntity product = item.getProduct();
            Long categoryId = product.getCategory() == null ? null : product.getCategory().getId();
            return new Product(product.getId(), product.getName(), item.getUnitPrice(), categoryId);
        }).toList();
        return new Order(entity.getId(), items, entity.getTotal());
    }
}
