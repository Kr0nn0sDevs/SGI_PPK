package model;

import java.util.ArrayList;
import java.util.List;

public class Producto {
    public String id, nombre, marca, descripcion, codigoBarras, categoria;
    public double precioVenta;
    public int stockMinimo = 5;
    public boolean activo = true;
    public List<Variante> variantes = new ArrayList<>();

    public int stockTotal() {
        return variantes.stream().mapToInt(v -> v.stock).sum();
    }
    public int stock(String talla, String color) {
        return variantes.stream()
            .filter(v -> v.talla.equals(talla) && v.color.equals(color))
            .mapToInt(v -> v.stock).findFirst().orElse(0);
    }
    public boolean stockBajo() {
        return variantes.stream().anyMatch(v -> v.stock > 0 && v.stock < stockMinimo);
    }
    @Override public String toString() {
        return nombre + " [" + marca + "]";
    }

    public static class Variante {
        public String talla, color;
        public int stock;
        public Variante() {}
        public Variante(String talla, String color, int stock) {
            this.talla = talla; this.color = color; this.stock = stock;
        }
        @Override public String toString() {
            return "T:" + talla + " C:" + color + " [" + stock + "]";
        }
    }
}
