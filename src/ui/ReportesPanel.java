package ui;

import dao.MovimientoDAO;
import dao.VentaDAO;
import export.CsvExporter;
import model.Movimiento;
import model.Venta;
import service.ContabilidadService;
import util.Fmt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportesPanel extends JPanel {
    private final VentaDAO vDao = new VentaDAO();
    private final MovimientoDAO mDao = new MovimientoDAO();
    private final ContabilidadService cs = new ContabilidadService();

    public ReportesPanel() { initUI(); }

    private void initUI() {
        setLayout(new BorderLayout()); setBackground(Colores.FONDO);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Colores.F_BOLD);
        tabs.addTab("Historial de Ventas",     panelVentas());
        tabs.addTab("Movimientos Inventario",  panelMovimientos());
        tabs.addTab("Reporte de Ingresos",     panelIngresos());
        add(tabs);
    }

    private JPanel panelVentas() {
        JTable t = Ui.tabla("Folio","Fecha","Vendedor","Subtotal","Desc","IVA","Total","Método","Estado");
        DefaultTableModel m = Ui.modelo(t);
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        p.setBackground(Colores.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        filtros.setBackground(Color.WHITE);
        filtros.setBorder(Ui.panelBorde("Filtros"));
        JSpinner spD = new JSpinner(new SpinnerDateModel());
        JSpinner spH = new JSpinner(new SpinnerDateModel());
        spD.setEditor(new JSpinner.DateEditor(spD,"dd/MM/yyyy"));
        spH.setEditor(new JSpinner.DateEditor(spH,"dd/MM/yyyy"));
        JTextField tfV = Ui.campo(12);
        JButton btnF  = Ui.boton("Filtrar", Colores.PRIMARIO);
        JButton btnCx = Ui.boton("❌ Cancelar venta", Colores.PELIGRO);
        JButton btnEx = Ui.boton("📥 CSV", Colores.ACENTO);
        filtros.add(Ui.etiqueta("Desde:")); filtros.add(spD);
        filtros.add(Ui.etiqueta("Hasta:")); filtros.add(spH);
        filtros.add(Ui.etiqueta("Vendedor:")); filtros.add(tfV);
        filtros.add(btnF); filtros.add(btnCx); filtros.add(btnEx);

        Runnable cargar = () -> {
            LocalDateTime desde = toDate(spD), hasta = toDate(spH).withHour(23).withMinute(59);
            String vend = tfV.getText().trim().toLowerCase();
            m.setRowCount(0);
            for(Venta v : vDao.porPeriodo(desde, hasta)) {
                if(!vend.isEmpty() && !v.nombreEmpleado.toLowerCase().contains(vend)) continue;
                m.addRow(new Object[]{ v.folio, Fmt.dt(v.fechaHora), v.nombreEmpleado,
                    Fmt.moneda(v.subtotal), Fmt.moneda(v.descuento), Fmt.moneda(v.iva),
                    Fmt.moneda(v.total), v.metodoPago, v.estado });
            }
        };
        btnF.addActionListener(e -> cargar.run());
        cargar.run();

        btnCx.addActionListener(e -> {
            int r = t.getSelectedRow(); if(r<0){ JOptionPane.showMessageDialog(p,"Selecciona una venta."); return; }
            String folio = (String)m.getValueAt(r,0);
            Venta v = vDao.listar().stream().filter(x->x.folio.equals(folio)).findFirst().orElse(null);
            if(v==null||v.estado!=Venta.Estado.CONFIRMADA){
                JOptionPane.showMessageDialog(p,"Solo se cancelan ventas confirmadas."); return; }
            String just = JOptionPane.showInputDialog(p,"Justificación de cancelación:");
            if(just==null||just.isBlank()){ JOptionPane.showMessageDialog(p,"Justificación obligatoria."); return; }
            try{ new service.VentaService().cancelar(v.id, just);
                JOptionPane.showMessageDialog(p,"✅ Cancelada. Stock repuesto.");
                cargar.run();
            } catch(Exception ex){ JOptionPane.showMessageDialog(p,"Error: "+ex.getMessage()); }
        });

        btnEx.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("reports");
            fc.setSelectedFile(new File("ventas_"+LocalDate.now()+".csv"));
            if(fc.showSaveDialog(p)!=JFileChooser.APPROVE_OPTION) return;
            try{ CsvExporter.exportarVentas(fc.getSelectedFile().getAbsolutePath(),
                    vDao.listar());
                JOptionPane.showMessageDialog(p,"✅ Exportado.");
            } catch(Exception ex){ JOptionPane.showMessageDialog(p,"Error: "+ex.getMessage()); }
        });

        p.add(filtros, BorderLayout.NORTH);
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelMovimientos() {
        JTable t = Ui.tabla("Fecha","Producto","Talla","Color","Cantidad","Tipo","Concepto");
        DefaultTableModel m = Ui.modelo(t);
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        p.setBackground(Colores.FONDO);
        JButton btn = Ui.boton("Cargar movimientos", Colores.PRIMARIO);
        btn.addActionListener(e -> {
            m.setRowCount(0);
            for(Movimiento mv : mDao.listar())
                m.addRow(new Object[]{ Fmt.dt(mv.fechaHora), mv.nombreProducto,
                    mv.talla, mv.color, mv.cantidad, mv.tipo, mv.concepto });
        });
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tb.setBackground(Colores.FONDO); tb.add(btn);
        p.add(tb, BorderLayout.NORTH);
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        return p;
    }

    private JPanel panelIngresos() {
        JTable t = Ui.tabla("Periodo","Ingresos","Costo Ventas","Util. Bruta","Gastos","Util. Neta");
        DefaultTableModel m = Ui.modelo(t);
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        p.setBackground(Colores.FONDO);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        top.setBackground(Colores.FONDO);
        JComboBox<String> cb = Ui.combo("Diario","Semanal","Mensual");
        JButton btnG = Ui.boton("Generar", Colores.PRIMARIO);
        JButton btnEx = Ui.boton("📥 CSV", Colores.ACENTO);
        top.add(Ui.etiqueta("Agrupación:")); top.add(cb);
        top.add(btnG); top.add(btnEx);

        btnG.addActionListener(e -> {
            m.setRowCount(0);
            LocalDateTime inicio = LocalDateTime.now().minusMonths(3);
            LocalDateTime fin = LocalDateTime.now();
            String ag = (String)cb.getSelectedItem();
            LocalDateTime cur = inicio;
            while(!cur.isAfter(fin)) {
                LocalDateTime next = "Mensual".equals(ag) ? cur.plusMonths(1)
                                   : "Semanal".equals(ag) ? cur.plusWeeks(1) : cur.plusDays(1);
                LocalDateTime hasta = next.minusNanos(1);
                double ing = cs.ingresos(cur, hasta);
                if(ing > 0) {
                    double cost = cs.costoVentas(cur, hasta);
                    double gas = cs.gastos(cur, hasta);
                    String lbl = "Mensual".equals(ag)
                        ? cur.getMonth().toString()+" "+cur.getYear()
                        : "Semanal".equals(ag)
                        ? "Sem "+cur.toLocalDate()
                        : cur.toLocalDate().toString();
                    m.addRow(new Object[]{ lbl, Fmt.moneda(ing), Fmt.moneda(cost),
                        Fmt.moneda(ing-cost), Fmt.moneda(gas), Fmt.moneda(ing-cost-gas) });
                }
                cur = next;
            }
        });

        btnEx.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("reports");
            fc.setSelectedFile(new File("ingresos_"+LocalDate.now()+".csv"));
            if(fc.showSaveDialog(p)!=JFileChooser.APPROVE_OPTION) return;
            try {
                CsvExporter.exportarEstadoResultados(fc.getSelectedFile().getAbsolutePath(),
                    cs, LocalDateTime.now().minusMonths(3), LocalDateTime.now());
                JOptionPane.showMessageDialog(p,"✅ Exportado.");
            } catch(Exception ex){ JOptionPane.showMessageDialog(p,"Error: "+ex.getMessage()); }
        });

        p.add(top, BorderLayout.NORTH);
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        return p;
    }

    private LocalDateTime toDate(JSpinner sp) {
        return ((java.util.Date)sp.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }
}
