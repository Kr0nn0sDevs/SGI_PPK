package ui;

import dao.ProductoDAO;
import model.Producto;
import service.InventarioService;
import util.Fmt;
import util.Json;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventarioPanel extends JPanel {
    private final JTable tabla = Ui.tabla("ID","Nombre","Marca","Categoría","Precio","Stock","Mín.","Estado");
    private final ProductoDAO dao = new ProductoDAO();
    private final InventarioService svc = new InventarioService();

    public InventarioPanel() { initUI(); cargar(); }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(Colores.FONDO);

        // Ocultar columna ID
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.setBackground(Colores.FONDO);
        JButton btnNuevo   = Ui.boton("➕ Nuevo",   Colores.ACENTO);
        JButton btnEditar  = Ui.boton("✏ Editar",  Colores.PRIMARIO);
        JButton btnBaja    = Ui.boton("🗑 Baja",    Colores.PELIGRO);
        JButton btnEntrada = Ui.boton("📥 Entrada stock", new Color(100,60,160));
        JButton btnRef     = Ui.botonSecundario("🔄");
        toolbar.add(btnNuevo); toolbar.add(btnEditar); toolbar.add(btnBaja);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnEntrada); toolbar.add(btnRef);

        btnNuevo.addActionListener(e   -> formulario(null));
        btnEditar.addActionListener(e  -> { int r=tabla.getSelectedRow(); if(r<0)return;
            formulario(dao.porId((String)Ui.modelo(tabla).getValueAt(r,0))); });
        btnBaja.addActionListener(e    -> { int r=tabla.getSelectedRow(); if(r<0)return;
            if(JOptionPane.showConfirmDialog(this,"¿Dar de baja?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
            { dao.bajaLogica((String)Ui.modelo(tabla).getValueAt(r,0)); cargar(); } });
        btnEntrada.addActionListener(e -> entradaStock());
        btnRef.addActionListener(e     -> cargar());

        // Panel alertas
        JPanel pAlertas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pAlertas.setBackground(new Color(255,250,230));
        List<Producto> al = svc.alertasStockBajo();
        if (!al.isEmpty()) {
            StringBuilder sb = new StringBuilder("⚠  Stock bajo: ");
            al.forEach(p -> sb.append(p.nombre).append("  "));
            JLabel lbl = new JLabel(sb.toString());
            lbl.setFont(Colores.F_BOLD); lbl.setForeground(Colores.ALERTA);
            pAlertas.add(lbl);
        }

        add(toolbar, BorderLayout.NORTH);
        add(Ui.scroll(tabla), BorderLayout.CENTER);
        add(pAlertas, BorderLayout.SOUTH);
    }

    private void cargar() {
        DefaultTableModel m = Ui.modelo(tabla); m.setRowCount(0);
        for (Producto p : dao.listar())
            m.addRow(new Object[]{ p.id, p.nombre, p.marca, p.categoria,
                Fmt.moneda(p.precioVenta), p.stockTotal(), p.stockMinimo,
                p.activo ? "Activo" : "Baja" });
    }

    private void formulario(Producto prod) {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),
            prod==null?"Nuevo producto":"Editar producto", true);
        d.setSize(440,360); d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfNombre   = Ui.campo(20); if(prod!=null) tfNombre.setText(prod.nombre);
        JTextField tfMarca    = Ui.campo(20); if(prod!=null) tfMarca.setText(prod.marca);
        JTextField tfDesc     = Ui.campo(20); if(prod!=null) tfDesc.setText(prod.descripcion);
        JTextField tfCodigo   = Ui.campo(20); if(prod!=null) tfCodigo.setText(prod.codigoBarras);
        JTextField tfPrecio   = Ui.campo(10); if(prod!=null) tfPrecio.setText(String.valueOf(prod.precioVenta));
        JTextField tfCat      = Ui.campo(20); if(prod!=null) tfCat.setText(prod.categoria); else tfCat.setText("General");
        JTextField tfStockMin = Ui.campo(6);  if(prod!=null) tfStockMin.setText(String.valueOf(prod.stockMinimo)); else tfStockMin.setText("5");

        String[][] campos = {{"Nombre:*",""},{"Marca:*",""},{"Descripción:",""},
                             {"Código barras:",""},{"Precio venta:*",""},{"Categoría:",""},{"Stock mínimo:",""}};
        JTextField[] tfs = {tfNombre, tfMarca, tfDesc, tfCodigo, tfPrecio, tfCat, tfStockMin};
        for (int i = 0; i < campos.length; i++) {
            g.gridx=0; g.gridy=i; g.weightx=0.3; p.add(Ui.etiqueta(campos[i][0]), g);
            g.gridx=1; g.weightx=0.7; p.add(tfs[i], g);
        }

        JButton btnOk = Ui.boton("Guardar", Colores.ACENTO);
        g.gridx=0; g.gridy=campos.length; g.gridwidth=2; p.add(btnOk, g);
        btnOk.addActionListener(ev -> {
            if(tfNombre.getText().isBlank()||tfMarca.getText().isBlank()||tfPrecio.getText().isBlank()){
                JOptionPane.showMessageDialog(d,"Nombre, marca y precio son obligatorios."); return; }
            Producto pr = prod!=null ? prod : new Producto();
            if(pr.id==null) pr.id = Json.newId("P");
            pr.nombre=tfNombre.getText().trim(); pr.marca=tfMarca.getText().trim();
            pr.descripcion=tfDesc.getText().trim(); pr.codigoBarras=tfCodigo.getText().trim();
            pr.categoria=tfCat.getText().trim();
            try { pr.precioVenta=Double.parseDouble(tfPrecio.getText().trim()); }
            catch(Exception ex){ JOptionPane.showMessageDialog(d,"Precio inválido."); return; }
            try { pr.stockMinimo=Integer.parseInt(tfStockMin.getText().trim()); } catch(Exception ex){ pr.stockMinimo=5; }
            dao.guardar(pr); d.dispose(); cargar();
        });
        d.add(p); d.setVisible(true);
    }

    private void entradaStock() {
        List<Producto> prods = dao.listarActivos();
        if(prods.isEmpty()){ JOptionPane.showMessageDialog(this,"No hay productos activos."); return; }
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Entrada de Stock",true);
        d.setSize(380,280); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Producto> cbProd = new JComboBox<>(prods.toArray(new Producto[0]));
        cbProd.setFont(Colores.F_NORMAL);
        JTextField tfTalla = Ui.campo(8), tfColor = Ui.campo(8),
                   tfCant  = Ui.campo(6), tfCosto = Ui.campo(10);

        Object[][] rows = {{"Producto:", cbProd},{"Talla:", tfTalla},{"Color:", tfColor},
                           {"Cantidad:", tfCant},{"Costo unitario:", tfCosto}};
        for (int i = 0; i < rows.length; i++) {
            g.gridx=0; g.gridy=i; p.add(Ui.etiqueta((String)rows[i][0]), g);
            g.gridx=1; p.add((Component)rows[i][1], g);
        }
        JButton btn = Ui.boton("Registrar entrada", Colores.ACENTO);
        g.gridx=0; g.gridy=rows.length; g.gridwidth=2; p.add(btn, g);
        btn.addActionListener(ev -> {
            Producto pr = (Producto)cbProd.getSelectedItem();
            try {
                svc.entrada(pr.id, tfTalla.getText().trim(), tfColor.getText().trim(),
                    Integer.parseInt(tfCant.getText().trim()),
                    Double.parseDouble(tfCosto.getText().trim()));
                JOptionPane.showMessageDialog(d,"✅ Entrada registrada.");
                d.dispose(); cargar();
            } catch(Exception ex){ JOptionPane.showMessageDialog(d,"Error: "+ex.getMessage()); }
        });
        d.add(p); d.setVisible(true);
    }
}
