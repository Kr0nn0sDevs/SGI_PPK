package service;

import dao.*;
import model.*;
import util.Json;
import util.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class InventarioService {
    private final ProductoDAO pDAO = new ProductoDAO();
    private final MovimientoDAO mDAO = new MovimientoDAO();
    private final LoteDAO lDAO = new LoteDAO();

    public void entrada(String idProd, String talla, String color, int cant, double costo) throws Exception {
        Producto p = pDAO.porId(idProd);
        if (p == null) throw new Exception("Producto no encontrado");
        boolean existe = false;
        for (Producto.Variante v : p.variantes)
            if (v.talla.equals(talla) && v.color.equals(color)) { v.stock += cant; existe = true; break; }
        if (!existe) p.variantes.add(new Producto.Variante(talla, color, cant));
        pDAO.guardar(p);

        Lote l = new Lote();
        l.id = Json.newId("L"); l.idProducto = idProd; l.talla = talla; l.color = color;
        l.fechaEntrada = LocalDate.now(); l.cantidadInicial = cant;
        l.cantidadDisponible = cant; l.costoUnitario = costo;
        lDAO.guardar(l);

        Movimiento m = new Movimiento();
        m.id = Json.newId("M"); m.idProducto = idProd; m.nombreProducto = p.nombre;
        m.talla = talla; m.color = color; m.cantidad = cant;
        m.tipo = Movimiento.Tipo.ENTRADA; m.concepto = "Entrada de mercancía";
        m.idEmpleado = Session.get().idUsuario(); m.costoUnitario = costo;
        mDAO.guardar(m);
    }

    public List<Producto> alertasStockBajo() {
        return pDAO.listarActivos().stream().filter(Producto::stockBajo).collect(Collectors.toList());
    }
}
