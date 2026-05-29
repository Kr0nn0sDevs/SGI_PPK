package ui;

import service.InventarioService;
import model.Producto;
import util.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame extends JFrame {
    private final JLabel lblAlerta = new JLabel("");
    private final InventarioService invSvc = new InventarioService();

    public MainFrame() {
        setTitle("Estilo Sport — Sistema de Gestión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 700));
        initUI();
        iniciarAlertaTimer();
    }

    private void initUI() {
        // Barra superior
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Colores.PRIMARIO);
        topBar.setBorder(BorderFactory.createEmptyBorder(8,14,8,14));

        JLabel appLbl = new JLabel("👟 ESTILO SPORT");
        appLbl.setFont(Colores.F_GRANDE); appLbl.setForeground(Color.WHITE);
        topBar.add(appLbl, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(Colores.PRIMARIO);
        lblAlerta.setFont(Colores.F_BOLD); lblAlerta.setForeground(new Color(255,220,60));
        right.add(lblAlerta);
        JLabel user = new JLabel("👤 " + Session.get().nombre()
            + " [" + Session.get().usuario().rol + "]");
        user.setFont(Colores.F_NORMAL); user.setForeground(Color.WHITE);
        right.add(user);
        JButton btnSalir = Ui.botonSecundario("Salir");
        btnSalir.setForeground(Color.WHITE); btnSalir.setBackground(new Color(200,60,60));
        btnSalir.setBorder(BorderFactory.createEmptyBorder(4,12,4,12));
        btnSalir.addActionListener(e -> { Session.get().logout(); dispose(); new LoginFrame().setVisible(true); });
        right.add(btnSalir);
        topBar.add(right, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Colores.F_BOLD);
        tabs.setBackground(Colores.FONDO);
        tabs.addTab("🛒 Punto de Venta", new POSPanel());
        tabs.addTab("📦 Inventario",     new InventarioPanel());
        if (Session.get().isAdmin()) {
            tabs.addTab("👥 Empleados",    new EmpleadosPanel());
            tabs.addTab("📊 Contabilidad", new ContabilidadPanel());
            tabs.addTab("📈 Dashboard",    new DashboardPanel());
            tabs.addTab("📋 Reportes",     new ReportesPanel());
        }
        add(tabs, BorderLayout.CENTER);

        tabs.addTab("🔑 Usuarios", new UsuariosPanel());
    }

    private void iniciarAlertaTimer() {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                List<Producto> al = invSvc.alertasStockBajo();
                SwingUtilities.invokeLater(() ->
                    lblAlerta.setText(al.isEmpty() ? "" : "⚠ " + al.size() + " producto(s) stock bajo"));
            }
        }, 0, 30_000);
    }
}
