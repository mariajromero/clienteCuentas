package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping
    public ResponseEntity<?> generarReporte(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String clienteId) {
        
        try {
            // Validar parámetros
            if (clienteId == null || clienteId.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El parámetro clienteId es requerido");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            if (fecha == null || fecha.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "El parámetro fecha es requerido. Formato: fechaInicio,fechaFin (yyyy-MM-dd)");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Parsear rango de fechas
            String[] fechas = fecha.split(",");
            if (fechas.length != 2) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "Formato de fecha incorrecto. Use: fechaInicio,fechaFin (yyyy-MM-dd)");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            
            Date fechaInicio;
            Date fechaFin;
            try {
                // Parsear fechas
                Date fechaInicioTemp = sdf.parse(fechas[0].trim());
                Date fechaFinTemp = sdf.parse(fechas[1].trim());
                
                // Establecer hora inicio del día para fechaInicio (00:00:00.000)
                Calendar calInicio = Calendar.getInstance();
                calInicio.setTime(fechaInicioTemp);
                calInicio.set(Calendar.HOUR_OF_DAY, 0);
                calInicio.set(Calendar.MINUTE, 0);
                calInicio.set(Calendar.SECOND, 0);
                calInicio.set(Calendar.MILLISECOND, 0);
                fechaInicio = calInicio.getTime();
                
                // Establecer hora fin del día para fechaFin (23:59:59.999)
                Calendar calFin = Calendar.getInstance();
                calFin.setTime(fechaFinTemp);
                calFin.set(Calendar.HOUR_OF_DAY, 23);
                calFin.set(Calendar.MINUTE, 59);
                calFin.set(Calendar.SECOND, 59);
                calFin.set(Calendar.MILLISECOND, 999);
                fechaFin = calFin.getTime();
                
            } catch (ParseException e) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "Formato de fecha inválido. Use: yyyy-MM-dd");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            if (fechaInicio.after(fechaFin)) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "La fecha de inicio debe ser anterior a la fecha de fin");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Buscar cuentas del cliente
            List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
            
            if (cuentas.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("mensaje", "No se encontraron cuentas para el cliente especificado");
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }

            // Construir el reporte
            Map<String, Object> reporte = new HashMap<>();
            reporte.put("clienteId", clienteId);
            reporte.put("fechaInicio", sdf.format(fechaInicio));
            reporte.put("fechaFin", sdf.format(fechaFin));
            reporte.put("fechaGeneracion", new Date());

            List<Map<String, Object>> cuentasReporte = new ArrayList<>();

            for (Cuenta cuenta : cuentas) {
                Map<String, Object> cuentaInfo = new HashMap<>();
                cuentaInfo.put("numeroCuenta", cuenta.getNumeroCuenta());
                cuentaInfo.put("tipoCuenta", cuenta.getTipoCuenta());
                cuentaInfo.put("saldoInicial", cuenta.getSaldoInicial());
                cuentaInfo.put("saldoActual", cuenta.getSaldoInicial());
                cuentaInfo.put("estado", cuenta.getEstado());

                // Buscar movimientos en el rango de fechas
                List<Movimiento> movimientos = movimientoRepository.findByCuentaAndFechaMovimientoBetween(
                        cuenta.getNumeroCuenta(), fechaInicio, fechaFin);

                List<Map<String, Object>> movimientosInfo = movimientos.stream()
                        .map(m -> {
                            Map<String, Object> mov = new HashMap<>();
                            mov.put("numeroMovimiento", m.getNumeroMovimiento());
                            mov.put("fechaMovimiento", m.getFechaMovimiento());
                            mov.put("tipoMovimiento", m.getTipoMovimiento());
                            mov.put("valor", m.getValor());
                            mov.put("saldo", m.getSaldo());
                            return mov;
                        })
                        .collect(Collectors.toList());

                cuentaInfo.put("movimientos", movimientosInfo);
                cuentaInfo.put("totalMovimientos", movimientos.size());
                
                // Calcular totales
                BigDecimal totalDebitos = movimientos.stream()
                        .filter(m -> m.getValor().compareTo(BigDecimal.ZERO) < 0)
                        .map(m -> m.getValor().abs())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal totalCreditos = movimientos.stream()
                        .filter(m -> m.getValor().compareTo(BigDecimal.ZERO) > 0)
                        .map(Movimiento::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                cuentaInfo.put("totalDebitos", totalDebitos);
                cuentaInfo.put("totalCreditos", totalCreditos);

                cuentasReporte.add(cuentaInfo);
            }

            reporte.put("cuentas", cuentasReporte);
            reporte.put("totalCuentas", cuentas.size());

            return new ResponseEntity<>(reporte, HttpStatus.OK);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al generar el reporte: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

