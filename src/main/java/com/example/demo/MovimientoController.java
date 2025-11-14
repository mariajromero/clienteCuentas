package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    /**
     * Recalculates the saldo for a movimiento based on all previous movimientos
     * @param cuenta The cuenta
     * @param movimiento The movimiento to calculate saldo for
     * @param currentCuentaSaldo The current saldo of the cuenta (before this movimiento update)
     * @param oldMovimientoValor The old valor of this movimiento (before update)
     */
    private BigDecimal calculateSaldoAfterMovimiento(Cuenta cuenta, Movimiento movimiento, BigDecimal currentCuentaSaldo, BigDecimal oldMovimientoValor) {
        // Get all movimientos for this cuenta ordered by date
        List<Movimiento> allMovimientos = movimientoRepository.findByCuentaOrderByFechaMovimientoAsc(cuenta);
        
        // Calculate sum of all movimientos using the old valor for this movimiento
        BigDecimal sumOfAllMovimientos = BigDecimal.ZERO;
        for (Movimiento m : allMovimientos) {
            if (m.getNumeroMovimiento().equals(movimiento.getNumeroMovimiento())) {
                sumOfAllMovimientos = sumOfAllMovimientos.add(oldMovimientoValor);
            } else {
                sumOfAllMovimientos = sumOfAllMovimientos.add(m.getValor());
            }
        }
        
        // Calculate original initial balance
        BigDecimal initialBalance = currentCuentaSaldo.subtract(sumOfAllMovimientos);
        
        // Now calculate balance up to and including this movimiento (using new valor)
        BigDecimal runningBalance = initialBalance;
        for (Movimiento m : allMovimientos) {
            if (m.getNumeroMovimiento().equals(movimiento.getNumeroMovimiento())) {
                runningBalance = runningBalance.add(movimiento.getValor());
                break;
            } else {
                runningBalance = runningBalance.add(m.getValor());
            }
        }
        
        return runningBalance;
    }

    @PostMapping
    public ResponseEntity<Movimiento> createMovimiento(@RequestBody Movimiento movimiento) {
        // Validate that cuenta is provided
        if (movimiento.getCuenta() == null || movimiento.getCuenta().getNumeroCuenta() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Find the cuenta
        Optional<Cuenta> cuentaOptional = cuentaRepository.findById(movimiento.getCuenta().getNumeroCuenta());
        if (cuentaOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Cuenta cuenta = cuentaOptional.get();

        // Validate that valor is provided
        if (movimiento.getValor() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Calculate new saldo: current saldoInicial + valor (valor can be positive or negative)
        BigDecimal currentSaldo = cuenta.getSaldoInicial();
        BigDecimal newSaldo = currentSaldo.add(movimiento.getValor());

        // Validate that saldo is not negative (saldo no disponible)
        if (newSaldo.compareTo(BigDecimal.ZERO) < 0) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Saldo no disponible");
            return new ResponseEntity("Saldo no disponible", HttpStatus.BAD_REQUEST);
        }

        // Set fechaMovimiento if not provided
        if (movimiento.getFechaMovimiento() == null) {
            movimiento.setFechaMovimiento(new Date());
        }

        // Set the cuenta and new saldo in movimiento
        movimiento.setCuenta(cuenta);
        movimiento.setSaldo(newSaldo);

        // Update cuenta's saldoInicial to reflect the new balance
        cuenta.setSaldoInicial(newSaldo);

        // Save both cuenta and movimiento (transaction is tracked)
        cuentaRepository.save(cuenta);
        Movimiento savedMovimiento = movimientoRepository.save(movimiento);

        return new ResponseEntity<>(savedMovimiento, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Movimiento>> getAllMovimientos() {
        List<Movimiento> movimientos = movimientoRepository.findAll();
        return new ResponseEntity<>(movimientos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movimiento> getMovimientoById(@PathVariable Long id) {
        Optional<Movimiento> movimiento = movimientoRepository.findById(id);
        return movimiento.map(m -> new ResponseEntity<>(m, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movimiento> updateMovimiento(@PathVariable Long id, @RequestBody Movimiento movimientoDetails) {
        Optional<Movimiento> movimientoOptional = movimientoRepository.findById(id);
        
        if (movimientoOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Movimiento movimiento = movimientoOptional.get();
        Cuenta cuenta = movimiento.getCuenta();
        BigDecimal oldValor = movimiento.getValor();

        // Update movimiento fields
        if (movimientoDetails.getFechaMovimiento() != null) {
            movimiento.setFechaMovimiento(movimientoDetails.getFechaMovimiento());
        }
        if (movimientoDetails.getTipoMovimiento() != null) {
            movimiento.setTipoMovimiento(movimientoDetails.getTipoMovimiento());
        }

        // Handle valor update and recalculate saldo
        if (movimientoDetails.getValor() != null) {
            BigDecimal newValor = movimientoDetails.getValor();
            BigDecimal currentSaldo = cuenta.getSaldoInicial();
            
            // Update movimiento valor first so we can recalculate saldo
            movimiento.setValor(newValor);
            
            // Recalculate the saldo after this movimiento (using old valor for calculation)
            BigDecimal movimientoSaldo = calculateSaldoAfterMovimiento(cuenta, movimiento, currentSaldo, oldValor);
            movimiento.setSaldo(movimientoSaldo);
            
            // Calculate the difference and adjust cuenta's saldo
            BigDecimal valorDifference = newValor.subtract(oldValor);
            BigDecimal updatedCuentaSaldo = currentSaldo.add(valorDifference);
            
            // Validate that saldo is not negative (saldo no disponible)
            if (updatedCuentaSaldo.compareTo(BigDecimal.ZERO) < 0) {
                // Revert the movimiento valor change
                movimiento.setValor(oldValor);
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "Saldo no disponible");
                return new ResponseEntity("Saldo no disponible", HttpStatus.BAD_REQUEST);
            }
            
            // Update cuenta's saldo to reflect the new current balance
            cuenta.setSaldoInicial(updatedCuentaSaldo);
            cuentaRepository.save(cuenta);
        }

        // Handle cuenta change (if provided)
        if (movimientoDetails.getCuenta() != null && movimientoDetails.getCuenta().getNumeroCuenta() != null) {
            Long newCuentaId = movimientoDetails.getCuenta().getNumeroCuenta();
            if (!newCuentaId.equals(cuenta.getNumeroCuenta())) {
                // Revert from old cuenta
                BigDecimal currentSaldo = cuenta.getSaldoInicial();
                cuenta.setSaldoInicial(currentSaldo.subtract(movimiento.getValor()));
                cuentaRepository.save(cuenta);
                
                // Apply to new cuenta
                Optional<Cuenta> newCuentaOptional = cuentaRepository.findById(newCuentaId);
                if (newCuentaOptional.isEmpty()) {
                    // Revert the old cuenta change
                    cuenta.setSaldoInicial(currentSaldo);
                    cuentaRepository.save(cuenta);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                Cuenta newCuenta = newCuentaOptional.get();
                BigDecimal newCuentaSaldo = newCuenta.getSaldoInicial().add(movimiento.getValor());
                
                // Validate that saldo is not negative (saldo no disponible)
                if (newCuentaSaldo.compareTo(BigDecimal.ZERO) < 0) {
                    // Revert the old cuenta change
                    cuenta.setSaldoInicial(currentSaldo);
                    cuentaRepository.save(cuenta);
                    Map<String, String> error = new HashMap<>();
                    error.put("mensaje", "Saldo no disponible");
                    return new ResponseEntity("Saldo no disponible", HttpStatus.BAD_REQUEST);
                }
                
                newCuenta.setSaldoInicial(newCuentaSaldo);
                cuentaRepository.save(newCuenta);
                
                movimiento.setCuenta(newCuenta);
                movimiento.setSaldo(newCuentaSaldo);
            }
        }
        
        Movimiento updatedMovimiento = movimientoRepository.save(movimiento);
        return new ResponseEntity<>(updatedMovimiento, HttpStatus.OK);
    }
}

