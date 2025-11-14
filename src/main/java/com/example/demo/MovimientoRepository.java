package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuentaOrderByFechaMovimientoAsc(Cuenta cuenta);
    
    @Query(value = "SELECT * FROM movimiento WHERE numero_cuenta = :numeroCuenta AND fecha_movimiento >= :fechaInicio AND fecha_movimiento <= :fechaFin ORDER BY fecha_movimiento ASC", nativeQuery = true)
    List<Movimiento> findByCuentaAndFechaMovimientoBetween(@Param("numeroCuenta") Long numeroCuenta, @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
}

