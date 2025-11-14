package com.example.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long numeroMovimiento;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date fechaMovimiento;

  @Column(nullable = false, length = 20)
  private String tipoMovimiento;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal valor;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal saldo;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "numero_cuenta", nullable = false)
  private Cuenta cuenta;
}
