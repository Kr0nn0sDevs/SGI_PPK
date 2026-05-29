package ui;

import dao.UsuarioDAO;
import model.Usuario;
import util.Session;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField tfUser = Ui.campo(16);
    private final JPasswordField tfPass = new JPasswordField(16);
    private final JLabel lblError = new JLabel(" ");
    private final UsuarioDAO dao = new UsuarioDAO();

    public LoginFrame() {
        setTitle("Estilo Sport — Acceso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(380, 300);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Colores.FONDO);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,12,8,12);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel logo = new JLabel("👟 ESTILO SPORT", SwingConstants.CENTER);
        logo.setFont(Colores.F_GRANDE); logo.setForeground(Colores.PRIMARIO);
        JLabel sub = new JLabel("Sistema de Gestión", SwingConstants.CENTER);
        sub.setFont(Colores.F_NORMAL); sub.setForeground(Colores.TEXTO);

        g.gridx=0; g.gridy=0; g.gridwidth=2; root.add(logo, g);
        g.gridy=1; root.add(sub, g);
        g.gridwidth=1; g.gridy=2;
        g.gridx=0; root.add(Ui.etiqueta("Usuario:"), g);
        g.gridx=1; root.add(tfUser, g);
        g.gridy=3; g.gridx=0; root.add(Ui.etiqueta("Contraseña:"), g);
        g.gridx=1;
        tfPass.setFont(Colores.F_NORMAL);
        tfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE),
            BorderFactory.createEmptyBorder(4,6,4,6)));
        root.add(tfPass, g);

        JButton btn = Ui.boton("Ingresar", Colores.PRIMARIO);
        btn.setPreferredSize(new Dimension(200, 38));
        g.gridy=4; g.gridx=0; g.gridwidth=2; root.add(btn, g);

        lblError.setForeground(Colores.PELIGRO);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        lblError.setFont(Colores.F_NORMAL);
        g.gridy=5; root.add(lblError, g);

        btn.addActionListener(e -> login());
        tfPass.addActionListener(e -> login());

        add(root);
    }

    private void login() {
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { lblError.setText("Ingresa usuario y contraseña"); return; }
        Usuario u = dao.porUsuario(user);
        if (u == null || !u.activo) { lblError.setText("Usuario no encontrado"); return; }
        if (u.intentosFallidos >= 5) { lblError.setText("Cuenta bloqueada"); return; }
        if (!u.password.equals(pass)) {
            u.intentosFallidos++;
            dao.guardar(u);
            lblError.setText("Contraseña incorrecta (" + (5-u.intentosFallidos) + " intentos)");
            return;
        }
        u.intentosFallidos = 0;
        dao.guardar(u);
        Session.get().login(u);
        dispose();
        new MainFrame().setVisible(true);
    }
}
