package dao;

import model.Producto;
import model.Producto.Variante;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    private static final String F = "data/inventario.json";

    public List<Producto> listar() {
        List<Producto> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public List<Producto> listarActivos() {
        List<Producto> r = new ArrayList<>();
        for (Producto p : listar()) if (p.activo) r.add(p);
        return r;
    }

    public Producto porId(String id) {
        return listar().stream().filter(p -> p.id.equals(id)).findFirst().orElse(null);
    }

    public Producto porCodigo(String cod) {
        return listarActivos().stream().filter(p -> cod.equals(p.codigoBarras)).findFirst().orElse(null);
    }

    public List<Producto> buscar(String texto) {
        String t = texto.toLowerCase();
        List<Producto> r = new ArrayList<>();
        for (Producto p : listarActivos())
            if (p.nombre.toLowerCase().contains(t) || p.marca.toLowerCase().contains(t)
                || p.codigoBarras.contains(t)) r.add(p);
        return r;
    }

    public void guardar(Producto p) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(p.id));
        arr.add(to(p));
        Json.writeArray(F, arr);
    }

    public void bajaLogica(String id) {
        Producto p = porId(id);
        if (p != null) { p.activo = false; guardar(p); }
    }

    public long count() { return listar().size(); }

    private JObj to(Producto p) {
        JObj o = new JObj();
        o.put("id", p.id); o.put("nombre", p.nombre); o.put("marca", p.marca);
        o.put("descripcion", p.descripcion); o.put("codigoBarras", p.codigoBarras);
        o.put("categoria", p.categoria); o.put("precioVenta", p.precioVenta);
        o.put("stockMinimo", p.stockMinimo); o.put("activo", p.activo);
        JArray vs = new JArray();
        for (Variante v : p.variantes) {
            JObj vj = new JObj();
            vj.put("talla", v.talla); vj.put("color", v.color); vj.put("stock", v.stock);
            vs.add(vj);
        }
        o.put("variantes", vs);
        return o;
    }

    private Producto from(JObj o) {
        Producto p = new Producto();
        p.id = o.getStr("id"); p.nombre = o.getStr("nombre"); p.marca = o.getStr("marca");
        p.descripcion = o.getStr("descripcion"); p.codigoBarras = o.getStr("codigoBarras");
        p.categoria = o.getStr("categoria"); p.precioVenta = o.getNum("precioVenta");
        p.stockMinimo = o.getInt("stockMinimo"); p.activo = o.getBool("activo");
        for (Object v : o.getArr("variantes")) {
            JObj vj = (JObj)v;
            p.variantes.add(new Variante(vj.getStr("talla"), vj.getStr("color"), vj.getInt("stock")));
        }
        return p;
    }
}
