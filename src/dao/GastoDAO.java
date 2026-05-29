package dao;

import model.Gasto;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {
    private static final String F = "data/gastos.json";

    public List<Gasto> listar() {
        List<Gasto> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public void guardar(Gasto g) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(g.id));
        arr.add(to(g));
        Json.writeArray(F, arr);
    }

    private JObj to(Gasto g) {
        JObj o = new JObj();
        o.put("id", g.id); o.put("fecha", g.fecha.toString());
        o.put("concepto", g.concepto); o.put("categoria", g.categoria);
        o.put("monto", g.monto); o.put("registradoPor", g.registradoPor);
        return o;
    }

    private Gasto from(JObj o) {
        Gasto g = new Gasto();
        g.id = o.getStr("id"); g.fecha = LocalDate.parse(o.getStr("fecha"));
        g.concepto = o.getStr("concepto"); g.categoria = o.getStr("categoria");
        g.monto = o.getNum("monto"); g.registradoPor = o.getStr("registradoPor");
        return g;
    }
}
