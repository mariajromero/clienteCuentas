package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    List<Cuenta> findByClienteId(String clienteId);
    
    @Modifying
    @Query(value = "INSERT INTO cliente_cuentas (cliente_id, cuenta_id) VALUES (:clienteId, :cuentaId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertClienteCuenta(@Param("clienteId") String clienteId, @Param("cuentaId") Long cuentaId);
}

