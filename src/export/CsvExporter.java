package export;

import model.Venta;
import service.ContabilidadService;
import service.ContabilidadService.Asiento;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

public class CsvExporter {

    public static void exportarLibroDiario(String ruta, ContabilidadService cs,
                                            LocalDateTime desde, LocalDateTime hasta) throws Exception {
        new File("reports").mkdirs();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            // para que Excel lo abra bien con acentos
            pw.print('\uFEFF');
            pw.println("LIBRO DIARIO");
            pw.println("Periodo:," + desde.toLocalDate() + " al " + hasta.toLocalDate());
            pw.println();
            pw.println("Fecha,Folio,Cuenta / Concepto,Debe,Haber");

            List<Asiento> asientos = cs.libroDiario(desde, hasta);
            double sumaDebe = 0, sumaHaber = 0;
            for (Asiento a : asientos) {
                pw.printf("%s,%s,%s,%.2f,%n", a.fecha, a.folio, a.cuentaCargo, a.debe);
                pw.printf("%s,%s,    %s,,%.2f%n", a.fecha, a.folio, a.cuentaAbono1, a.haber1);
                pw.printf("%s,%s,    %s,,%.2f%n", a.fecha, a.folio, a.cuentaAbono2, a.haber2);
                pw.println();
                sumaDebe += a.debe; sumaHaber += a.haber1 + a.haber2;
            }
            pw.printf(",,SUMAS IGUALES,%.2f,%.2f%n", sumaDebe, sumaHaber);
        }
    }

    public static void exportarEstadoResultados(String ruta, ContabilidadService cs,
                                                 LocalDateTime desde, LocalDateTime hasta) throws Exception {
        new File("reports").mkdirs();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            pw.print('\uFEFF');
            double ing = cs.ingresos(desde, hasta);
            double costo = cs.costoVentas(desde, hasta);
            double gastos = cs.gastos(desde, hasta);
            double uBruta = ing - costo;
            double uNeta = uBruta - gastos;

            pw.println("ESTADO DE RESULTADOS");
            pw.println("Periodo:," + desde.toLocalDate() + " al " + hasta.toLocalDate());
            pw.println();
            pw.println("Concepto,Monto");
            pw.println("INGRESOS,");
            pw.printf("  Ventas netas,%.2f%n", ing);
            pw.printf("Total ingresos,%.2f%n", ing);
            pw.println();
            pw.println("COSTO DE VENTAS,");
            pw.printf("  Costo mercancia vendida (PEPS),%.2f%n", costo);
            pw.printf("Total costo de ventas,%.2f%n", costo);
            pw.println();
            pw.printf("UTILIDAD BRUTA,%.2f%n", uBruta);
            pw.println();
            pw.println("GASTOS DE OPERACION,");
            // Desglose por categoría
            cs.gastoDAO().listar().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    g -> g.categoria, java.util.stream.Collectors.summingDouble(g -> g.monto)))
                .forEach((cat, total) -> pw.printf("  %s,%.2f%n", cat, total));
            pw.printf("Total gastos,%.2f%n", gastos);
            pw.println();
            pw.printf("%s,%.2f%n", uNeta >= 0 ? "UTILIDAD NETA" : "PERDIDA NETA", Math.abs(uNeta));
        }
    }

    public static void exportarVentas(String ruta, List<Venta> ventas) throws Exception {
        new File("reports").mkdirs();
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            pw.print('\uFEFF');
            pw.println("Folio,Fecha/Hora,Vendedor,Subtotal,Descuento,IVA,Total,Metodo Pago,Estado");
            for (Venta v : ventas)
                pw.printf("%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%s,%s%n",
                    v.folio, v.fechaHora, v.nombreEmpleado,
                    v.subtotal, v.descuento, v.iva, v.total, v.metodoPago, v.estado);
        }
    }

    public static void exportarBalanceGeneral(String ruta, ContabilidadService cs,
                                               LocalDateTime hasta) throws Exception {
        new File("reports").mkdirs();
        LocalDateTime inicio = LocalDateTime.of(2000,1,1,0,0);
        double ing = cs.ingresos(inicio, hasta);
        double gastos = cs.gastos(inicio, hasta);
        double capital = ing - gastos;

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(ruta), StandardCharsets.UTF_8))) {
            pw.print('\uFEFF');
            pw.println("BALANCE GENERAL");
            pw.println("Al:," + hasta.toLocalDate());
            pw.println();
            pw.println("ACTIVO,");
            pw.println("  Activo Circulante,");
            pw.printf("    Caja / Bancos,%.2f%n", ing);
            pw.printf("  Total Activo Circulante,%.2f%n", ing);
            pw.printf("TOTAL ACTIVO,%.2f%n", ing);
            pw.println();
            pw.println("PASIVO Y CAPITAL,");
            pw.printf("  Gastos acumulados,%.2f%n", gastos);
            pw.printf("  Capital contable,%.2f%n", capital);
            pw.printf("TOTAL PASIVO + CAPITAL,%.2f%n", gastos + capital);
        }
    }
}
