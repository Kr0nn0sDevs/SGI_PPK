package service;

import dao.*;
import model.*;
import util.Fmt;
import util.Json;
import util.Session;

import java.time.LocalDateTime;
import java.util.List;

public class VentaService {
    private final VentaDAO vDAO = new VentaDAO();
    private final ProductoDAO pDAO = new ProductoDAO();
    private final MovimientoDAO mDAO = new MovimientoDAO();
    private final LoteDAO lDAO = new LoteDAO();

    public Venta registrar(Venta venta) throws Exception {
        // Validar stock
        for (ItemVenta it : venta.items) {
            Producto p = pDAO.porId(it.idProducto);
            if (p == null) throw new Exception("Producto no encontrado: " + it.idProducto);
            if (p.stock(it.talla, it.color) < it.cantidad)
                throw new Exception("Stock insuficiente: " + p.nombre
                    + " T:" + it.talla + " C:" + it.color);
        }
        venta.id = Json.newId("V");
        venta.folio = Fmt.folio(vDAO.count() + 1);
        venta.fechaHora = LocalDateTime.now();
        venta.calcularTotales();

        for (ItemVenta it : venta.items) {
            Producto p = pDAO.porId(it.idProducto);
            descontarPEPS(p, it);
            Movimiento m = new Movimiento();
            m.id = Json.newId("M"); m.idProducto = it.idProducto;
            m.nombreProducto = it.nombreProducto; m.talla = it.talla; m.color = it.color;
            m.cantidad = it.cantidad; m.tipo = Movimiento.Tipo.SALIDA;
            m.concepto = "Venta " + venta.folio; m.idEmpleado = venta.idEmpleado;
            m.idVenta = venta.id; m.costoUnitario = it.costoUnitario;
            mDAO.guardar(m);
        }
        vDAO.guardar(venta);
        return venta;
    }

    public void cancelar(String idVenta, String justif) throws Exception {
        Venta v = vDAO.porId(idVenta);
        if (v == null) throw new Exception("Venta no encontrada");
        if (v.estado != Venta.Estado.CONFIRMADA) throw new Exception("Solo se cancelan ventas confirmadas");
        for (ItemVenta it : v.items) {
            Producto p = pDAO.porId(it.idProducto);
            if (p != null) {
                for (Producto.Variante var : p.variantes)
                    if (var.talla.equals(it.talla) && var.color.equals(it.color)) {
                        var.stock += it.cantidad; break;
                    }
                pDAO.guardar(p);
            }
            Movimiento m = new Movimiento();
            m.id = Json.newId("M"); m.idProducto = it.idProducto;
            m.nombreProducto = it.nombreProducto; m.talla = it.talla; m.color = it.color;
            m.cantidad = it.cantidad; m.tipo = Movimiento.Tipo.CANCELACION;
            m.concepto = "Cancelación " + v.folio; m.idVenta = idVenta;
            m.idEmpleado = Session.get().idUsuario();
            mDAO.guardar(m);
        }
        v.estado = Venta.Estado.CANCELADA;
        v.justificacion = justif;
        v.canceladoPor = Session.get().nombre();
        vDAO.guardar(v);
    }

    private void descontarPEPS(Producto p, ItemVenta it) {
        List<Lote> lotes = lDAO.disponibles(p.id, it.talla, it.color);
        int rest = it.cantidad; double costoTotal = 0; int cnt = 0;
        for (Lote l : lotes) {
            if (rest <= 0) break;
            int usar = Math.min(rest, l.cantidadDisponible);
            costoTotal += usar * l.costoUnitario; cnt += usar;
            l.cantidadDisponible -= usar; lDAO.guardar(l); rest -= usar;
        }
        if (cnt > 0) it.costoUnitario = costoTotal / cnt;
        for (Producto.Variante v : p.variantes)
            if (v.talla.equals(it.talla) && v.color.equals(it.color)) { v.stock -= it.cantidad; break; }
        pDAO.guardar(p);
    }
}
