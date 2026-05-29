package dao;

import model.Empleado;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {
    private static final String F = "data/empleados.json";

    public List<Empleado> listar() {
        List<Empleado> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public List<Empleado> listarActivos() {
        List<Empleado> r = new ArrayList<>();
        for (Empleado e : listar()) if (e.activo) r.add(e);
        return r;
    }

    public Empleado porId(String id) {
        return listar().stream().filter(e -> e.id.equals(id)).findFirst().orElse(null);
    }

    public void guardar(Empleado e) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(e.id));
        arr.add(to(e));
        Json.writeArray(F, arr);
    }

    public void baja(String id) {
        Empleado e = porId(id);
        if (e != null) { e.activo = false; guardar(e); }
    }

    private JObj to(Empleado e) {
        JObj o = new JObj();
        o.put("id", e.id); o.put("nombre", e.nombre); o.put("curp", e.curp);
        o.put("puesto", e.puesto); o.put("correo", e.correo);
        o.put("fechaIngreso", e.fechaIngreso != null ? e.fechaIngreso : "");
        o.put("sueldoBase", e.sueldoBase);
        o.put("porcentajeComision", e.porcentajeComision); o.put("activo", e.activo);
        return o;
    }

    private Empleado from(JObj o) {
        Empleado e = new Empleado();
        e.id = o.getStr("id"); e.nombre = o.getStr("nombre"); e.curp = o.getStr("curp");
        e.puesto = o.getStr("puesto"); e.correo = o.getStr("correo");
        e.fechaIngreso = o.getStr("fechaIngreso");
        e.sueldoBase = o.getNum("sueldoBase");
        e.porcentajeComision = o.getNum("porcentajeComision"); e.activo = o.getBool("activo");
        return e;
    }
}
