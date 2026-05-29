package model;

public class ItemVenta {
    public String idProducto, nombreProducto, talla, color;
    public int cantidad;
    public double precioUnitario, descuentoUnitario, subtotal, costoUnitario;

    public void calcular() {
        subtotal = (precioUnitario - descuentoUnitario) * cantidad;
    }
}
