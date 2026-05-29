package ui;

import dao.GastoDAO;
import export.CsvExporter;
import model.Gasto;
import service.ContabilidadService;
import util.Fmt;
import util.Json;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ContabilidadPanel extends JPanel {
    private final JTable tGastos = Ui.tabla("ID","Fecha","Concepto","Categoría","Monto","Registrado por");
    private final GastoDAO gDao = new GastoDAO();
    private final ContabilidadService cs = new ContabilidadService();
    private final JSpinner spD = new JSpinner(new SpinnerDateModel());
    private final JSpinner spH = new JSpinner(new SpinnerDateModel());

    public ContabilidadPanel() { initUI(); cargarGastos(); }

    private void initUI() {
        setLayout(new BorderLayout(8,8)); setBackground(Colores.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        tGastos.getColumnModel().getColumn(0).setMinWidth(0);
        tGastos.getColumnModel().getColumn(0).setMaxWidth(0);

        // Panel periodo
        JPanel pPer = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        pPer.setBorder(Ui.panelBorde("Periodo y Exportación"));
        pPer.setBackground(Color.WHITE);
        spD.setEditor(new JSpinner.DateEditor(spD,"dd/MM/yyyy"));
        spH.setEditor(new JSpinner.DateEditor(spH,"dd/MM/yyyy"));
        JButton btnResumen  = Ui.boton("📊 Resumen", Colores.PRIMARIO);
        JButton btnDiario   = Ui.boton("📥 Libro Diario CSV", new Color(0,130,80));
        JButton btnER       = Ui.boton("📥 Estado Resultados CSV", new Color(100,60,160));
        JButton btnBal      = Ui.boton("📥 Balance General CSV", new Color(160,80,0));
        pPer.add(Ui.etiqueta("Desde:")); pPer.add(spD);
        pPer.add(Ui.etiqueta("Hasta:")); pPer.add(spH);
        pPer.add(btnResumen); pPer.add(btnDiario); pPer.add(btnER); pPer.add(btnBal);

        btnResumen.addActionListener(e -> resumen());
        btnDiario.addActionListener(e  -> exportar("diario"));
        btnER.addActionListener(e      -> exportar("er"));
        btnBal.addActionListener(e     -> exportar("balance"));

        // Panel gastos
        JPanel pG = new JPanel(new BorderLayout(4,4));
        pG.setBorder(Ui.panelBorde("Registro de Gastos"));
        pG.setBackground(Color.WHITE);
        JButton btnNuevo = Ui.boton("➕ Registrar gasto", Colores.ACENTO);
        btnNuevo.addActionListener(e -> formGasto());
        JPanel tbG = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tbG.setBackground(Color.WHITE); tbG.add(btnNuevo);
        pG.add(tbG, BorderLayout.NORTH);
        pG.add(Ui.scroll(tGastos), BorderLayout.CENTER);

        add(pPer, BorderLayout.NORTH);
        add(pG, BorderLayout.CENTER);
    }

    private void cargarGastos() {
        DefaultTableModel m = Ui.modelo(tGastos); m.setRowCount(0);
        for(Gasto g : gDao.listar())
            m.addRow(new Object[]{ g.id, g.fecha, g.concepto, g.categoria,
                Fmt.moneda(g.monto), g.registradoPor });
    }

    private LocalDateTime desde() {
        return toDate(spD).atStartOfDay();
    }
    private LocalDateTime hasta() {
        return toDate(spH).atTime(23,59,59);
    }
    private LocalDate toDate(JSpinner sp) {
        return ((java.util.Date)sp.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void resumen() {
        LocalDateTime d = desde(), h = hasta();
        double ing=cs.ingresos(d,h), costo=cs.costoVentas(d,h),
               gas=cs.gastos(d,h), uB=ing-costo, uN=uB-gas;
        String txt = String.format(
            "RESUMEN FINANCIERO  [%s — %s]\n\n" +
            "Ingresos por ventas:        %12s\n" +
            "Costo de ventas (PEPS):     %12s\n" +
            "─────────────────────────────────\n" +
            "Utilidad bruta:             %12s\n\n" +
            "Gastos de operación:        %12s\n" +
            "─────────────────────────────────\n" +
            "Utilidad neta:              %12s\n" +
            "Margen neto:                %11.1f%%",
            desde().toLocalDate(), hasta().toLocalDate(),
            Fmt.moneda(ing),Fmt.moneda(costo),Fmt.moneda(uB),
            Fmt.moneda(gas),Fmt.moneda(uN),
            ing>0 ? uN/ing*100 : 0);
        JTextArea ta = new JTextArea(txt);
        ta.setFont(Colores.F_MONO); ta.setEditable(false);
        JOptionPane.showMessageDialog(this, ta, "Resumen financiero", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportar(String tipo) {
        new File("reports").mkdirs();
        JFileChooser fc = new JFileChooser("reports");
        fc.setSelectedFile(new File("reports/" + tipo + "_" + LocalDate.now() + ".csv"));
        if(fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            String ruta = fc.getSelectedFile().getAbsolutePath();
            switch(tipo) {
                case "diario":  CsvExporter.exportarLibroDiario(ruta, cs, desde(), hasta()); break;
                case "er":      CsvExporter.exportarEstadoResultados(ruta, cs, desde(), hasta()); break;
                case "balance": CsvExporter.exportarBalanceGeneral(ruta, cs, hasta()); break;
            }
            JOptionPane.showMessageDialog(this,"✅ Exportado: " + ruta);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void formGasto() {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Registrar Gasto",true);
        d.setSize(380,280); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(6,6,6,6); g.fill=GridBagConstraints.HORIZONTAL;

        JSpinner spFecha = new JSpinner(new SpinnerDateModel());
        spFecha.setEditor(new JSpinner.DateEditor(spFecha,"dd/MM/yyyy"));
        JTextField tfConc = Ui.campo(20);
        JComboBox<String> cbCat = Ui.combo("Gastos administración","Gastos venta",
            "Servicios públicos","Renta","Nómina","Otros");
        JTextField tfMonto = Ui.campo(12);

        Object[][] rows = {{"Fecha:",spFecha},{"Concepto:*",tfConc},{"Categoría:",cbCat},{"Monto:*",tfMonto}};
        for(int i=0;i<rows.length;i++){
            g.gridx=0; g.gridy=i; p.add(Ui.etiqueta((String)rows[i][0]),g);
            g.gridx=1; p.add((Component)rows[i][1],g);
        }
        JButton btn = Ui.boton("Guardar", Colores.ACENTO);
        g.gridx=0; g.gridy=rows.length; g.gridwidth=2; p.add(btn,g);
        btn.addActionListener(ev -> {
            if(tfConc.getText().isBlank()||tfMonto.getText().isBlank()){
                JOptionPane.showMessageDialog(d,"Concepto y monto obligatorios."); return; }
            Gasto ga = new Gasto();
            ga.id = Json.newId("G"); ga.concepto = tfConc.getText().trim();
            ga.categoria = (String)cbCat.getSelectedItem();
            ga.registradoPor = Session.get().nombre();
            ga.fecha = ((java.util.Date)spFecha.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            try{ ga.monto=Double.parseDouble(tfMonto.getText().trim()); }
            catch(Exception ex){ JOptionPane.showMessageDialog(d,"Monto inválido."); return; }
            gDao.guardar(ga); d.dispose(); cargarGastos();
        });
        d.add(p); d.setVisible(true);
    }
}
