package ui;

import dao.ProductoDAO;
import model.*;
import service.VentaService;
import util.Fmt;
import util.Json;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {
    private final JTextField tfBusqueda = Ui.campo(22);
    private final JTable tCarrito = Ui.tabla("ID","Producto","Talla","Color","Precio","Desc","Cant","Subtotal");
    private final JLabel lblSub   = new JLabel("Subtotal: $0.00");
    private final JLabel lblIva   = new JLabel("IVA 16%: $0.00");
    private final JLabel lblTotal = new JLabel("TOTAL: $0.00");
    private final JLabel lblCambio= new JLabel("Cambio: $0.00");
    private final JTextField tfDesc     = Ui.campo(6);
    private final JPasswordField tfPin  = new JPasswordField(8);
    private final JTextField tfEfectivo = Ui.campo(10);
    private final JComboBox<String> cbPago = Ui.combo("Efectivo","Tarjeta Débito","Tarjeta Crédito","Transferencia");

    private final List<ItemVenta> items = new ArrayList<>();
    private final ProductoDAO pDAO = new ProductoDAO();
    private final VentaService svc = new VentaService();

    public POSPanel() { initUI(); tfDesc.setText("0"); tfEfectivo.setText("0"); }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setBackground(Colores.FONDO);

        // Panel búsqueda
        JPanel pBusq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pBusq.setBorder(Ui.panelBorde("Buscar producto (nombre, marca o código de barras)"));
        pBusq.setBackground(Color.WHITE);
        pBusq.add(Ui.etiqueta("Buscar:")); pBusq.add(tfBusqueda);
        JButton btnBuscar = Ui.boton("🔍 Buscar", Colores.PRIMARIO);
        pBusq.add(btnBuscar);
        btnBuscar.addActionListener(e -> buscar());
        tfBusqueda.addActionListener(e -> buscar());
        add(pBusq, BorderLayout.NORTH);

        // Carrito
        JPanel pCarrito = new JPanel(new BorderLayout(4,4));
        pCarrito.setBorder(Ui.panelBorde("Carrito"));
        pCarrito.setBackground(Color.WHITE);
        // Ocultar columna ID
        tCarrito.getColumnModel().getColumn(0).setMinWidth(0);
        tCarrito.getColumnModel().getColumn(0).setMaxWidth(0);
        tCarrito.getColumnModel().getColumn(0).setWidth(0);
        JButton btnElim = Ui.boton("🗑 Quitar ítem", Colores.PELIGRO);
        btnElim.addActionListener(e -> quitarItem());
        JPanel botCarrito = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botCarrito.setBackground(Color.WHITE); botCarrito.add(btnElim);
        pCarrito.add(Ui.scroll(tCarrito), BorderLayout.CENTER);
        pCarrito.add(botCarrito, BorderLayout.SOUTH);
        add(pCarrito, BorderLayout.CENTER);

        // Panel cobro
        JPanel pCobro = new JPanel(new GridBagLayout());
        pCobro.setBorder(Ui.panelBorde("Cobro"));
        pCobro.setBackground(Color.WHITE);
        pCobro.setPreferredSize(new Dimension(270,0));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,8,5,8); g.fill = GridBagConstraints.HORIZONTAL;

        lblSub.setFont(Colores.F_NORMAL);
        lblIva.setFont(Colores.F_NORMAL);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotal.setForeground(Colores.ACENTO);
        lblCambio.setFont(Colores.F_BOLD); lblCambio.setForeground(Colores.PRIMARIO);

        tfPin.setFont(Colores.F_NORMAL);
        tfPin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE),
            BorderFactory.createEmptyBorder(4,6,4,6)));

        int row = 0;
        g.gridx=0; g.gridy=row; g.gridwidth=2; pCobro.add(lblSub, g); row++;
        g.gridy=row; pCobro.add(lblIva, g); row++;
        g.gridy=row; pCobro.add(lblTotal, g); row++;
        g.gridy=row; pCobro.add(new JSeparator(), g); row++;
        g.gridwidth=1;
        g.gridy=row; g.gridx=0; pCobro.add(Ui.etiqueta("Descuento %:"), g);
        g.gridx=1; pCobro.add(tfDesc, g); row++;
        g.gridy=row; g.gridx=0; pCobro.add(Ui.etiqueta("PIN admin:"), g);
        g.gridx=1; pCobro.add(tfPin, g); row++;
        g.gridy=row; g.gridx=0; pCobro.add(Ui.etiqueta("Método pago:"), g);
        g.gridx=1; pCobro.add(cbPago, g); row++;
        g.gridy=row; g.gridx=0; pCobro.add(Ui.etiqueta("Recibido:"), g);
        g.gridx=1; pCobro.add(tfEfectivo, g); row++;
        g.gridy=row; g.gridx=0; g.gridwidth=2; pCobro.add(lblCambio, g); row++;

        JButton btnCobrar = Ui.boton("✅  COBRAR", Colores.ACENTO);
        btnCobrar.setPreferredSize(new Dimension(220,42));
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.gridy=row; pCobro.add(btnCobrar, g); row++;
        JButton btnNueva = Ui.botonSecundario("🔄 Nueva venta");
        g.gridy=row; pCobro.add(btnNueva, g);

        btnCobrar.addActionListener(e -> cobrar());
        btnNueva.addActionListener(e -> limpiar());
        tfDesc.addActionListener(e -> actualizarTotales());
        tfEfectivo.addActionListener(e -> actualizarTotales());
        cbPago.addActionListener(e -> tfEfectivo.setEnabled("Efectivo".equals(cbPago.getSelectedItem())));

        add(pCobro, BorderLayout.EAST);
    }

    private void buscar() {
        String txt = tfBusqueda.getText().trim();
        if (txt.isEmpty()) return;
        List<Producto> lista;
        Producto xCod = pDAO.porCodigo(txt);
        lista = xCod != null ? List.of(xCod) : pDAO.buscar(txt);
        if (lista.isEmpty()) { JOptionPane.showMessageDialog(this,"Sin resultados."); return; }

        Producto p = lista.size() == 1 ? lista.get(0)
            : (Producto) JOptionPane.showInputDialog(this,"Selecciona:","Resultados",
                JOptionPane.PLAIN_MESSAGE, null, lista.toArray(), lista.get(0));
        if (p == null) return;

        List<Producto.Variante> conStock = new ArrayList<>();
        for (Producto.Variante v : p.variantes) if (v.stock > 0) conStock.add(v);
        if (conStock.isEmpty()) { JOptionPane.showMessageDialog(this,"Sin stock disponible."); return; }

        Producto.Variante v = conStock.size() == 1 ? conStock.get(0)
            : (Producto.Variante) JOptionPane.showInputDialog(this,"Selecciona talla/color:",
                p.nombre, JOptionPane.PLAIN_MESSAGE, null, conStock.toArray(), conStock.get(0));
        if (v == null) return;

        String cantStr = JOptionPane.showInputDialog(this,"Cantidad (disponible: "+v.stock+"):","1");
        if (cantStr == null) return;
        int cant;
        try { cant = Integer.parseInt(cantStr.trim()); } catch (Exception ex) { return; }
        if (cant <= 0 || cant > v.stock) { JOptionPane.showMessageDialog(this,"Cantidad inválida."); return; }

        ItemVenta it = new ItemVenta();
        it.idProducto = p.id; it.nombreProducto = p.nombre;
        it.talla = v.talla; it.color = v.color; it.cantidad = cant;
        it.precioUnitario = p.precioVenta; it.calcular();
        items.add(it);

        DefaultTableModel m = Ui.modelo(tCarrito);
        m.addRow(new Object[]{p.id, p.nombre, v.talla, v.color,
            Fmt.moneda(p.precioVenta), Fmt.moneda(0), cant, Fmt.moneda(it.subtotal)});
        actualizarTotales();
        tfBusqueda.setText("");
    }

    private void quitarItem() {
        int row = tCarrito.getSelectedRow();
        if (row < 0) return;
        items.remove(row);
        Ui.modelo(tCarrito).removeRow(row);
        actualizarTotales();
    }

    private void actualizarTotales() {
        double sub = items.stream().mapToDouble(i->i.subtotal).sum();
        double desc = 0;
        try { desc = sub * Double.parseDouble(tfDesc.getText().trim()) / 100; } catch (Exception ignored) {}
        double iva = (sub - desc) * 0.16;
        double total = sub - desc + iva;
        lblSub.setText("Subtotal: " + Fmt.moneda(sub));
        lblIva.setText("IVA 16%: " + Fmt.moneda(iva));
        lblTotal.setText("TOTAL: " + Fmt.moneda(total));
        try {
            double recibido = Double.parseDouble(tfEfectivo.getText().trim());
            lblCambio.setText("Cambio: " + Fmt.moneda(Math.max(0, recibido - total)));
        } catch (Exception ignored) {}
    }

    private void cobrar() {
        if (items.isEmpty()) { JOptionPane.showMessageDialog(this,"Carrito vacío."); return; }
        double desc = 0;
        try { desc = Double.parseDouble(tfDesc.getText().trim()); } catch (Exception ignored) {}
        if (desc > 0) {
            String pin = new String(tfPin.getPassword());
            dao.UsuarioDAO ud = new dao.UsuarioDAO();
            boolean ok = ud.listar().stream()
                .anyMatch(u -> u.rol == model.Usuario.Rol.ADMIN && u.password.equals(pin));
            if (!ok && !Session.get().isAdmin()) {
                JOptionPane.showMessageDialog(this,"PIN de administrador incorrecto."); return;
            }
        }
        Venta v = new Venta();
        v.idEmpleado = Session.get().idUsuario();
        v.nombreEmpleado = Session.get().nombre();
        v.items = new ArrayList<>(items);
        double sub = items.stream().mapToDouble(i->i.subtotal).sum();
        v.descuento = sub * desc / 100;
        v.metodoPago = (String) cbPago.getSelectedItem();
        try { v.montoEfectivo = Double.parseDouble(tfEfectivo.getText().trim()); } catch (Exception ignored) {}
        v.calcularTotales();
        v.cambio = Math.max(0, v.montoEfectivo - v.total);
        if ("Efectivo".equals(v.metodoPago) && v.montoEfectivo < v.total) {
            JOptionPane.showMessageDialog(this,"Monto recibido insuficiente."); return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Confirmar venta\nTotal: " + Fmt.moneda(v.total) + "\nCambio: " + Fmt.moneda(v.cambio),
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            svc.registrar(v);
            JOptionPane.showMessageDialog(this,
                "✅ Venta registrada\nFolio: " + v.folio
                + "\nTotal: " + Fmt.moneda(v.total)
                + "\nCambio: " + Fmt.moneda(v.cambio),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        items.clear(); Ui.modelo(tCarrito).setRowCount(0);
        tfBusqueda.setText(""); tfDesc.setText("0");
        tfEfectivo.setText("0"); tfPin.setText("");
        actualizarTotales();
    }
}
