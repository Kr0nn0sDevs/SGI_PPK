package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Ui {

    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setFont(Colores.F_BOLD);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        return b;
    }

    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFont(Colores.F_NORMAL);
        b.setBackground(Colores.SECUNDARIO);
        b.setForeground(Colores.PRIMARIO);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Colores.PRIMARIO));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JTextField campo(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(Colores.F_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return f;
    }

    public static JComboBox<String> combo(String... opts) {
        JComboBox<String> c = new JComboBox<>(opts);
        c.setFont(Colores.F_NORMAL);
        return c;
    }

    public static JLabel etiqueta(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(Colores.F_NORMAL);
        l.setForeground(Colores.TEXTO);
        return l;
    }

    public static JLabel titulo(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(Colores.F_TITULO);
        l.setForeground(Colores.PRIMARIO);
        return l;
    }

    public static JTable tabla(String... cols) {
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setFont(Colores.F_NORMAL);
        t.setRowHeight(24);
        t.setGridColor(Colores.BORDE);
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(Colores.SECUNDARIO);
        t.setSelectionForeground(Colores.TEXTO);
        t.getTableHeader().setFont(Colores.F_BOLD);
        t.getTableHeader().setBackground(Colores.PRIMARIO);
        t.getTableHeader().setForeground(Color.WHITE);
        // Filas alternadas
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 255));
                return this;
            }
        });
        return t;
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(Colores.BORDE));
        return sp;
    }

    public static Border panelBorde(String titulo) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Colores.BORDE), titulo,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            Colores.F_BOLD, Colores.PRIMARIO);
    }

    public static DefaultTableModel modelo(JTable t) {
        return (DefaultTableModel) t.getModel();
    }
}
