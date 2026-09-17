package com.example.project.repository;

import com.example.project.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;


public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    // 1. Consulta JPQL para filtrar pedidos por monto mayor a un valor dado
    @Query("SELECT p FROM Pedido p WHERE p.total > :minimo")
    List<Pedido> buscarPedidosMayoresA(@Param("minimo") BigDecimal minimo);

    // 2. JOIN FETCH para traer el Pedido con su Usuario en una sola consulta SQL (Evita problema N+1)
    @Query("SELECT p FROM Pedido p JOIN FETCH p.usuario WHERE p.usuario.id = :usuarioId")
    List<Pedido> buscarPedidosConUsuarioPorId(@Param("usuarioId") Long usuarioId);
}
