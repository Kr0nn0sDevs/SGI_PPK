package dao;

import model.Movimiento;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {
    private static final String F = "data/movimientos.json";

    public List<Movimiento> listar() {
        List<Movimiento> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public void guardar(Movimiento m) {
        JArray arr = Json.readArray(F);
        arr.add(to(m));
        Json.writeArray(F, arr);
    }

    private JObj to(Movimiento m) {
        JObj o = new JObj();
        o.put("id", m.id); o.put("fechaHora", m.fechaHora.toString());
        o.put("idProducto", m.idProducto); o.put("nombreProducto", m.nombreProducto);
        o.put("talla", m.talla != null ? m.talla : "");
        o.put("color", m.color != null ? m.color : "");
        o.put("cantidad", m.cantidad); o.put("tipo", m.tipo.name());
        o.put("concepto", m.concepto);
        o.put("idEmpleado", m.idEmpleado != null ? m.idEmpleado : "");
        o.put("idVenta", m.idVenta != null ? m.idVenta : "");
        o.put("costoUnitario", m.costoUnitario);
        return o;
    }

    private Movimiento from(JObj o) {
        Movimiento m = new Movimiento();
        m.id = o.getStr("id"); m.fechaHora = LocalDateTime.parse(o.getStr("fechaHora"));
        m.idProducto = o.getStr("idProducto"); m.nombreProducto = o.getStr("nombreProducto");
        m.talla = o.getStr("talla"); m.color = o.getStr("color");
        m.cantidad = o.getInt("cantidad"); m.tipo = Movimiento.Tipo.valueOf(o.getStr("tipo"));
        m.concepto = o.getStr("concepto"); m.idEmpleado = o.getStr("idEmpleado");
        m.idVenta = o.getStr("idVenta"); m.costoUnitario = o.getNum("costoUnitario");
        return m;
    }
}
