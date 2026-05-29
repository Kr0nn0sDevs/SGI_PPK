package dao;

import model.ItemVenta;
import model.Venta;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {
    private static final String F = "data/ventas.json";

    public List<Venta> listar() {
        List<Venta> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public Venta porId(String id) {
        return listar().stream().filter(v -> v.id.equals(id)).findFirst().orElse(null);
    }

    public List<Venta> porPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        List<Venta> r = new ArrayList<>();
        for (Venta v : listar())
            if (!v.fechaHora.isBefore(desde) && !v.fechaHora.isAfter(hasta)) r.add(v);
        return r;
    }

    public long count() { return listar().size(); }

    public void guardar(Venta v) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(v.id));
        arr.add(to(v));
        Json.writeArray(F, arr);
    }

    private JObj to(Venta v) {
        JObj o = new JObj();
        o.put("id", v.id); o.put("folio", v.folio);
        o.put("fechaHora", v.fechaHora.toString());
        o.put("idEmpleado", v.idEmpleado); o.put("nombreEmpleado", v.nombreEmpleado);
        o.put("subtotal", v.subtotal); o.put("descuento", v.descuento);
        o.put("iva", v.iva); o.put("total", v.total);
        o.put("metodoPago", v.metodoPago); o.put("montoEfectivo", v.montoEfectivo);
        o.put("cambio", v.cambio); o.put("estado", v.estado.name());
        o.put("justificacion", v.justificacion != null ? v.justificacion : "");
        o.put("canceladoPor", v.canceladoPor != null ? v.canceladoPor : "");
        JArray items = new JArray();
        for (ItemVenta it : v.items) {
            JObj ij = new JObj();
            ij.put("idProducto", it.idProducto); ij.put("nombreProducto", it.nombreProducto);
            ij.put("talla", it.talla); ij.put("color", it.color);
            ij.put("cantidad", it.cantidad); ij.put("precioUnitario", it.precioUnitario);
            ij.put("descuentoUnitario", it.descuentoUnitario);
            ij.put("subtotal", it.subtotal); ij.put("costoUnitario", it.costoUnitario);
            items.add(ij);
        }
        o.put("items", items);
        return o;
    }

    private Venta from(JObj o) {
        Venta v = new Venta();
        v.id = o.getStr("id"); v.folio = o.getStr("folio");
        v.fechaHora = LocalDateTime.parse(o.getStr("fechaHora"));
        v.idEmpleado = o.getStr("idEmpleado"); v.nombreEmpleado = o.getStr("nombreEmpleado");
        v.subtotal = o.getNum("subtotal"); v.descuento = o.getNum("descuento");
        v.iva = o.getNum("iva"); v.total = o.getNum("total");
        v.metodoPago = o.getStr("metodoPago"); v.montoEfectivo = o.getNum("montoEfectivo");
        v.cambio = o.getNum("cambio"); v.estado = Venta.Estado.valueOf(o.getStr("estado"));
        v.justificacion = o.getStr("justificacion"); v.canceladoPor = o.getStr("canceladoPor");
        for (Object i : o.getArr("items")) {
            JObj ij = (JObj)i;
            ItemVenta it = new ItemVenta();
            it.idProducto = ij.getStr("idProducto"); it.nombreProducto = ij.getStr("nombreProducto");
            it.talla = ij.getStr("talla"); it.color = ij.getStr("color");
            it.cantidad = ij.getInt("cantidad"); it.precioUnitario = ij.getNum("precioUnitario");
            it.descuentoUnitario = ij.getNum("descuentoUnitario");
            it.subtotal = ij.getNum("subtotal"); it.costoUnitario = ij.getNum("costoUnitario");
            v.items.add(it);
        }
        return v;
    }
}
