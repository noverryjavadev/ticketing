package co.apps.ticketing.repository.order;

import co.apps.ticketing.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.expireAt < :dateTime")
    List<Order> findByStatusAndExpireAtBefore(@Param("status") String status,
                                              @Param("dateTime") LocalDateTime dateTime);

    @Modifying
    @Transactional
    @Query(value = "UPDATE _orders SET status = :status, updated_at = NOW(), version = version + 1 " +
            "WHERE id = :orderId AND status = :expectedStatus AND version = :version",
            nativeQuery = true)
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("status") String status,
                          @Param("expectedStatus") String expectedStatus,
                          @Param("version") Integer version);
}
