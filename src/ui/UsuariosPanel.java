package ui;

import dao.UsuarioDAO;
import model.Usuario;
import util.Json;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UsuariosPanel extends JPanel {
    private final JTable tabla = Ui.tabla("ID","Nombre","Usuario","Rol","Activo","Intentos");
    private final UsuarioDAO dao = new UsuarioDAO();

    public UsuariosPanel() { initUI(); cargar(); }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        setBackground(Colores.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Ocultar columna ID
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        tb.setBackground(Colores.FONDO);
        JButton btnN = Ui.boton("➕ Nuevo usuario",    Colores.ACENTO);
        JButton btnE = Ui.boton("✏ Editar",            Colores.PRIMARIO);
        JButton btnD = Ui.boton("🔒 Desactivar",       Colores.PELIGRO);
        JButton btnR = Ui.boton("🔓 Desbloquear",      new Color(100,60,160));
        tb.add(btnN); tb.add(btnE); tb.add(btnD); tb.add(btnR);

        btnN.addActionListener(e -> form(null));
        btnE.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if(r < 0) return;
            form(dao.porUsuario((String) Ui.modelo(tabla).getValueAt(r, 2)));
        });
        btnD.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if(r < 0) return;
            Usuario u = dao.porUsuario((String) Ui.modelo(tabla).getValueAt(r, 2));
            if(u == null) return;
            if(u.usuario.equals(Session.get().usuario().usuario)) {
                JOptionPane.showMessageDialog(this, "No puedes desactivar tu propia cuenta.");
                return;
            }
            u.activo = !u.activo;
            dao.guardar(u);
            cargar();
        });
        btnR.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if(r < 0) return;
            Usuario u = dao.porUsuario((String) Ui.modelo(tabla).getValueAt(r, 2));
            if(u == null) return;
            u.intentosFallidos = 0;
            u.activo = true;
            dao.guardar(u);
            JOptionPane.showMessageDialog(this, "✅ Cuenta desbloqueada.");
            cargar();
        });

        add(tb, BorderLayout.NORTH);
        add(Ui.scroll(tabla), BorderLayout.CENTER);

        JLabel nota = new JLabel("  ℹ Los cambios de contraseña aplican en el siguiente inicio de sesión.");
        nota.setFont(Colores.F_NORMAL);
        nota.setForeground(new Color(100,100,120));
        add(nota, BorderLayout.SOUTH);
    }

    private void cargar() {
        DefaultTableModel m = Ui.modelo(tabla); m.setRowCount(0);
        for(Usuario u : dao.listar())
            m.addRow(new Object[]{ u.id, u.nombre, u.usuario,
                u.rol, u.activo ? "✅ Activo" : "🔒 Inactivo", u.intentosFallidos });
    }

    private void form(Usuario usr) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            usr == null ? "Nuevo usuario" : "Editar usuario", true);
        d.setSize(400, 320); d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7,7,7,7); g.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfNombre  = Ui.campo(18); if(usr!=null) tfNombre.setText(usr.nombre);
        JTextField tfUsuario = Ui.campo(18); if(usr!=null) tfUsuario.setText(usr.usuario);
        JPasswordField tfPass = new JPasswordField(18);
        tfPass.setFont(Colores.F_NORMAL);
        tfPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE),
            BorderFactory.createEmptyBorder(4,6,4,6)));
        JPasswordField tfPass2 = new JPasswordField(18);
        tfPass2.setFont(Colores.F_NORMAL);
        tfPass2.setBorder(tfPass.getBorder());
        JComboBox<String> cbRol = Ui.combo("ADMIN","VENDEDOR");
        if(usr != null) cbRol.setSelectedItem(usr.rol.name());

        JLabel lblInfo = new JLabel(usr != null ? "Deja contraseña vacía para no cambiarla" : "");
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(new Color(120,120,140));

        Object[][] rows = {
            {"Nombre completo:*", tfNombre},
            {"Nombre de usuario:*", tfUsuario},
            {"Contraseña:*", tfPass},
            {"Confirmar contraseña:*", tfPass2},
            {"Rol:", cbRol},
            {"", lblInfo}
        };
        for(int i = 0; i < rows.length; i++) {
            g.gridx=0; g.gridy=i; g.weightx=0.35;
            p.add(Ui.etiqueta((String)rows[i][0]), g);
            g.gridx=1; g.weightx=0.65;
            p.add((Component)rows[i][1], g);
        }

        JButton btnOk = Ui.boton("Guardar", Colores.ACENTO);
        g.gridx=0; g.gridy=rows.length; g.gridwidth=2;
        p.add(btnOk, g);

        btnOk.addActionListener(ev -> {
            String nombre  = tfNombre.getText().trim();
            String usuario = tfUsuario.getText().trim();
            String pass    = new String(tfPass.getPassword());
            String pass2   = new String(tfPass2.getPassword());

            if(nombre.isEmpty() || usuario.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Nombre y usuario son obligatorios."); return;
            }
            if(usr == null && pass.isEmpty()) {
                JOptionPane.showMessageDialog(d, "La contraseña es obligatoria."); return;
            }
            if(!pass.isEmpty() && !pass.equals(pass2)) {
                JOptionPane.showMessageDialog(d, "Las contraseñas no coinciden."); return;
            }
            // Verificar que el nombre de usuario no esté duplicado
            Usuario existente = dao.porUsuario(usuario);
            if(existente != null && (usr == null || !existente.id.equals(usr.id))) {
                JOptionPane.showMessageDialog(d, "Ese nombre de usuario ya existe."); return;
            }

            Usuario u = usr != null ? usr : new Usuario();
            if(u.id == null) u.id = Json.newId("U");
            u.nombre  = nombre;
            u.usuario = usuario;
            if(!pass.isEmpty()) u.password = pass;
            u.rol = Usuario.Rol.valueOf((String) cbRol.getSelectedItem());

            dao.guardar(u);
            d.dispose();
            cargar();
            JOptionPane.showMessageDialog(this, "✅ Usuario guardado correctamente.");
        });

        d.add(p);
        d.setVisible(true);
    }
}
