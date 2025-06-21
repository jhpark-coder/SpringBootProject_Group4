package com.creatorworks.nexus.order.repository;

import com.creatorworks.nexus.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
