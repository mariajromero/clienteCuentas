package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long numeroCuenta;

  @Column(nullable = false, length = 20)
  private String tipoCuenta;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal saldoInicial;

  @Column(nullable = false, length = 10)
  private String estado;

  @Column(name = "cliente_id", nullable = false, length = 50)
  private String clienteId;

}
