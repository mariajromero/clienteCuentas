package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    @Autowired
    private CuentaRepository cuentaRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<Cuenta> createCuenta(@RequestBody Cuenta cuenta) {
        Cuenta savedCuenta = cuentaRepository.save(cuenta);
        // Insertar en la tabla cliente_cuentas para que el otro microservicio pueda encontrarla
        cuentaRepository.insertClienteCuenta(savedCuenta.getClienteId(), savedCuenta.getNumeroCuenta());
        return new ResponseEntity<>(savedCuenta, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cuenta>> getAllCuentas() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        return new ResponseEntity<>(cuentas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuenta> getCuentaById(@PathVariable Long id) {
        Optional<Cuenta> cuenta = cuentaRepository.findById(id);
        return cuenta.map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Cuenta>> getCuentasByClienteId(@PathVariable String clienteId) {
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        return new ResponseEntity<>(cuentas, HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}/ids")
    public ResponseEntity<Map<String, List<Long>>> getCuentaIdsByClienteId(@PathVariable String clienteId) {
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        List<Long> cuentaIds = cuentas.stream()
                .map(Cuenta::getNumeroCuenta)
                .collect(Collectors.toList());
        Map<String, List<Long>> response = new HashMap<>();
        response.put("cuentaIds", cuentaIds);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cuenta> updateCuenta(@PathVariable Long id, @RequestBody Cuenta cuentaDetails) {
        Optional<Cuenta> cuentaOptional = cuentaRepository.findById(id);
        
        if (cuentaOptional.isPresent()) {
            Cuenta cuenta = cuentaOptional.get();
            cuenta.setTipoCuenta(cuentaDetails.getTipoCuenta());
            cuenta.setSaldoInicial(cuentaDetails.getSaldoInicial());
            cuenta.setEstado(cuentaDetails.getEstado());
            if (cuentaDetails.getClienteId() != null) {
                cuenta.setClienteId(cuentaDetails.getClienteId());
            }
            
            Cuenta updatedCuenta = cuentaRepository.save(cuenta);
            return new ResponseEntity<>(updatedCuenta, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}

