package service;

import dao.*;
import model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContabilidadService {
    private final VentaDAO vDAO = new VentaDAO();
    private final GastoDAO gDAO = new GastoDAO();

    public double ingresos(LocalDateTime d, LocalDateTime h) {
        return vDAO.porPeriodo(d,h).stream()
            .filter(v -> v.estado == Venta.Estado.CONFIRMADA)
            .mapToDouble(v -> v.total).sum();
    }

    public double costoVentas(LocalDateTime d, LocalDateTime h) {
        double c = 0;
        for (Venta v : vDAO.porPeriodo(d,h))
            if (v.estado == Venta.Estado.CONFIRMADA)
                for (ItemVenta it : v.items) c += it.costoUnitario * it.cantidad;
        return c;
    }

    public double gastos(LocalDateTime d, LocalDateTime h) {
        return gDAO.listar().stream()
            .filter(g -> {
                LocalDateTime gd = g.fecha.atStartOfDay();
                return !gd.isBefore(d) && !gd.isAfter(h);
            }).mapToDouble(g -> g.monto).sum();
    }

    public double utilidadBruta(LocalDateTime d, LocalDateTime h) { return ingresos(d,h) - costoVentas(d,h); }
    public double utilidadNeta(LocalDateTime d, LocalDateTime h)  { return utilidadBruta(d,h) - gastos(d,h); }

    public List<Asiento> libroDiario(LocalDateTime d, LocalDateTime h) {
        List<Asiento> l = new ArrayList<>();
        for (Venta v : vDAO.porPeriodo(d,h)) {
            if (v.estado != Venta.Estado.CONFIRMADA) continue;
            Asiento a = new Asiento();
            a.fecha = v.fechaHora.toLocalDate().toString();
            a.folio = v.folio; a.concepto = "Venta " + v.folio;
            a.cuentaCargo = v.metodoPago.contains("Efectivo") ? "Caja" : "Bancos";
            a.debe = v.total;
            a.cuentaAbono1 = "Ventas"; a.haber1 = v.subtotal - v.descuento;
            a.cuentaAbono2 = "IVA Trasladado"; a.haber2 = v.iva;
            l.add(a);
        }
        return l;
    }

    public VentaDAO ventaDAO() { return vDAO; }
    public GastoDAO gastoDAO() { return gDAO; }

    public static class Asiento {
        public String fecha, folio, concepto, cuentaCargo, cuentaAbono1, cuentaAbono2;
        public double debe, haber1, haber2;
    }
}
