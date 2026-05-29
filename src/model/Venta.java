package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    public enum Estado { CONFIRMADA, CANCELADA }
    public String id, folio, idEmpleado, nombreEmpleado;
    public String metodoPago, justificacion, canceladoPor;
    public LocalDateTime fechaHora = LocalDateTime.now();
    public List<ItemVenta> items = new ArrayList<>();
    public double subtotal, descuento, iva, total, montoEfectivo, cambio;
    public Estado estado = Estado.CONFIRMADA;

    public void calcularTotales() {
        subtotal = items.stream().mapToDouble(i -> i.subtotal).sum();
        iva = (subtotal - descuento) * 0.16;
        total = subtotal - descuento + iva;
    }
}
