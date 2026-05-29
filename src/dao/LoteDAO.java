package dao;

import model.Lote;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LoteDAO {
    private static final String F = "data/lotes.json";

    public List<Lote> listar() {
        List<Lote> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public List<Lote> disponibles(String idProd, String talla, String color) {
        List<Lote> r = new ArrayList<>();
        for (Lote l : listar())
            if (l.idProducto.equals(idProd) && l.talla.equals(talla)
                && l.color.equals(color) && l.cantidadDisponible > 0) r.add(l);
        r.sort(Comparator.comparing(l -> l.fechaEntrada));
        return r;
    }

    public void guardar(Lote l) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(l.id));
        arr.add(to(l));
        Json.writeArray(F, arr);
    }

    private JObj to(Lote l) {
        JObj o = new JObj();
        o.put("id", l.id); o.put("idProducto", l.idProducto);
        o.put("talla", l.talla); o.put("color", l.color);
        o.put("fechaEntrada", l.fechaEntrada.toString());
        o.put("cantidadInicial", l.cantidadInicial);
        o.put("cantidadDisponible", l.cantidadDisponible);
        o.put("costoUnitario", l.costoUnitario);
        return o;
    }

    private Lote from(JObj o) {
        Lote l = new Lote();
        l.id = o.getStr("id"); l.idProducto = o.getStr("idProducto");
        l.talla = o.getStr("talla"); l.color = o.getStr("color");
        l.fechaEntrada = LocalDate.parse(o.getStr("fechaEntrada"));
        l.cantidadInicial = o.getInt("cantidadInicial");
        l.cantidadDisponible = o.getInt("cantidadDisponible");
        l.costoUnitario = o.getNum("costoUnitario");
        return l;
    }
}
