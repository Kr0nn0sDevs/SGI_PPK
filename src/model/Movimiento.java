package model;

import java.time.LocalDateTime;

public class Movimiento {
    public enum Tipo { ENTRADA, SALIDA, AJUSTE, CANCELACION }
    public String id, idProducto, nombreProducto, talla, color;
    public String concepto, idEmpleado, idVenta;
    public int cantidad;
    public double costoUnitario;
    public Tipo tipo;
    public LocalDateTime fechaHora = LocalDateTime.now();
}
