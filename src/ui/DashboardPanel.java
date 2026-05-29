package ui;

import service.ContabilidadService;
import util.Fmt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;

public class DashboardPanel extends JPanel {
    private final ContabilidadService cs = new ContabilidadService();
    private final JLabel[] kpiVals = new JLabel[6];

    public DashboardPanel() { initUI(); }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        setBackground(Colores.FONDO);

        JLabel titulo = new JLabel("📈 Dashboard Financiero", SwingConstants.CENTER);
        titulo.setFont(Colores.F_GRANDE); titulo.setForeground(Colores.PRIMARIO);
        add(titulo, BorderLayout.NORTH);

        // Selector periodo
        JPanel pSel = new JPanel(new FlowLayout());
        pSel.setBackground(Colores.FONDO);
        JComboBox<String> cbPer = Ui.combo("Hoy","Esta semana","Este mes","Este año");
        cbPer.setSelectedIndex(2);
        JButton btnAct = Ui.boton("Actualizar", Colores.PRIMARIO);
        pSel.add(Ui.etiqueta("Periodo:")); pSel.add(cbPer); pSel.add(btnAct);

        // KPIs
        JPanel pKpi = new JPanel(new GridLayout(2,3,14,14));
        pKpi.setBackground(Colores.FONDO);
        pKpi.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        String[] nombres = {"Ingresos","Costo Ventas","Utilidad Bruta","Gastos","Utilidad Neta","Margen %"};
        Color[] cols = {Colores.PRIMARIO,new Color(160,60,0),Colores.ACENTO,
                        new Color(130,0,160),new Color(0,120,100),new Color(100,100,0)};
        for(int i=0;i<6;i++){
            JPanel card = new JPanel(new BorderLayout(4,4));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,4,0,0,cols[i]),
                BorderFactory.createEmptyBorder(12,14,12,14)));
            JLabel lbl = new JLabel(nombres[i], SwingConstants.CENTER);
            lbl.setFont(Colores.F_BOLD); lbl.setForeground(new Color(90,90,110));
            kpiVals[i] = new JLabel("—", SwingConstants.CENTER);
            kpiVals[i].setFont(new Font("Segoe UI", Font.BOLD, 22));
            kpiVals[i].setForeground(cols[i]);
            card.add(lbl, BorderLayout.NORTH); card.add(kpiVals[i], BorderLayout.CENTER);
            pKpi.add(card);
        }

        // Tabla últimas ventas
        JTable tVentas = Ui.tabla("Folio","Fecha","Vendedor","Total","Estado");
        DefaultTableModel tm = Ui.modelo(tVentas);

        Runnable actualizar = () -> {
            LocalDateTime[] r = rango((String)cbPer.getSelectedItem());
            double ing = cs.ingresos(r[0],r[1]);
            double costo = cs.costoVentas(r[0],r[1]);
            double gas = cs.gastos(r[0],r[1]);
            double uB = ing-costo, uN = uB-gas;
            kpiVals[0].setText(Fmt.moneda(ing));
            kpiVals[1].setText(Fmt.moneda(costo));
            kpiVals[2].setText(Fmt.moneda(uB));
            kpiVals[3].setText(Fmt.moneda(gas));
            kpiVals[4].setText(Fmt.moneda(uN));
            kpiVals[5].setText(String.format("%.1f%%", ing>0?uN/ing*100:0));
            tm.setRowCount(0);
            var ventas = cs.ventaDAO().listar();
            int desde = Math.max(0, ventas.size()-15);
            for(int i=ventas.size()-1; i>=desde; i--) {
                var v = ventas.get(i);
                tm.addRow(new Object[]{v.folio, Fmt.dt(v.fechaHora),
                    v.nombreEmpleado, Fmt.moneda(v.total), v.estado});
            }
        };
        btnAct.addActionListener(e -> actualizar.run());
        actualizar.run();

        JScrollPane spVentas = Ui.scroll(tVentas);
        spVentas.setBorder(Ui.panelBorde("Últimas ventas"));
        spVentas.setPreferredSize(new Dimension(0,220));

        JPanel center = new JPanel(new BorderLayout(8,8));
        center.setBackground(Colores.FONDO);
        center.add(pSel, BorderLayout.NORTH);
        center.add(pKpi, BorderLayout.CENTER);
        center.add(spVentas, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private LocalDateTime[] rango(String per) {
        LocalDateTime now = LocalDateTime.now();
        return switch(per) {
            case "Hoy"         -> new LocalDateTime[]{now.toLocalDate().atStartOfDay(), now};
            case "Esta semana" -> new LocalDateTime[]{now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(), now};
            case "Este mes"    -> new LocalDateTime[]{now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(), now};
            default            -> new LocalDateTime[]{now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay(), now};
        };
    }
}
