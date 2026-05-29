package ui;

import dao.EmpleadoDAO;
import dao.VentaDAO;
import model.Empleado;
import model.Venta;
import util.Fmt;
import util.Json;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmpleadosPanel extends JPanel {
    private final JTable tabla = Ui.tabla("ID","Nombre","CURP","Puesto","Sueldo","Comisión %","Correo","Estado");
    private final EmpleadoDAO dao = new EmpleadoDAO();

    public EmpleadosPanel() { initUI(); cargar(); }

    private void initUI() {
        setLayout(new BorderLayout(8,8)); setBackground(Colores.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        tabla.getColumnModel().getColumn(0).setMinWidth(0);
        tabla.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        tb.setBackground(Colores.FONDO);
        JButton btnN = Ui.boton("➕ Nuevo", Colores.ACENTO);
        JButton btnE = Ui.boton("✏ Editar", Colores.PRIMARIO);
        JButton btnB = Ui.boton("🗑 Baja", Colores.PELIGRO);
        JButton btnC = Ui.boton("💰 Comisiones", new Color(100,60,160));
        tb.add(btnN); tb.add(btnE); tb.add(btnB); tb.add(btnC);

        btnN.addActionListener(e -> form(null));
        btnE.addActionListener(e -> { int r=tabla.getSelectedRow(); if(r<0)return;
            form(dao.porId((String)Ui.modelo(tabla).getValueAt(r,0))); });
        btnB.addActionListener(e -> { int r=tabla.getSelectedRow(); if(r<0)return;
            if(JOptionPane.showConfirmDialog(this,"¿Dar de baja?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
            { dao.baja((String)Ui.modelo(tabla).getValueAt(r,0)); cargar(); }});
        btnC.addActionListener(e -> comisiones());

        add(tb, BorderLayout.NORTH);
        add(Ui.scroll(tabla), BorderLayout.CENTER);
    }

    private void cargar() {
        DefaultTableModel m = Ui.modelo(tabla); m.setRowCount(0);
        for (Empleado e : dao.listar())
            m.addRow(new Object[]{ e.id, e.nombre, e.curp, e.puesto,
                Fmt.moneda(e.sueldoBase), e.porcentajeComision+"%",
                e.correo, e.activo?"Activo":"Baja" });
    }

    private void form(Empleado emp) {
        JDialog d = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),
            emp==null?"Nuevo empleado":"Editar empleado",true);
        d.setSize(420,320); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(6,6,6,6); g.fill=GridBagConstraints.HORIZONTAL;

        JTextField tfN  = Ui.campo(20); if(emp!=null) tfN.setText(emp.nombre);
        JTextField tfC  = Ui.campo(20); if(emp!=null) tfC.setText(emp.curp);
        JTextField tfP  = Ui.campo(20); if(emp!=null) tfP.setText(emp.puesto);
        JTextField tfS  = Ui.campo(12); if(emp!=null) tfS.setText(String.valueOf(emp.sueldoBase));
        JTextField tfCo = Ui.campo(20); if(emp!=null) tfCo.setText(emp.correo);
        JTextField tfCm = Ui.campo(6);  if(emp!=null) tfCm.setText(String.valueOf(emp.porcentajeComision)); else tfCm.setText("0");

        Object[][] rows = {{"Nombre:*",tfN},{"CURP:",tfC},{"Puesto:",tfP},
                           {"Sueldo base:",tfS},{"Correo:",tfCo},{"Comisión %:",tfCm}};
        for(int i=0;i<rows.length;i++){
            g.gridx=0; g.gridy=i; p.add(Ui.etiqueta((String)rows[i][0]),g);
            g.gridx=1; p.add((Component)rows[i][1],g);
        }
        JButton btn = Ui.boton("Guardar", Colores.ACENTO);
        g.gridx=0; g.gridy=rows.length; g.gridwidth=2; p.add(btn,g);
        btn.addActionListener(ev -> {
            if(tfN.getText().isBlank()){ JOptionPane.showMessageDialog(d,"Nombre obligatorio."); return; }
            Empleado e = emp!=null?emp:new Empleado();
            if(e.id==null) e.id=Json.newId("E");
            e.nombre=tfN.getText().trim(); e.curp=tfC.getText().trim();
            e.puesto=tfP.getText().trim(); e.correo=tfCo.getText().trim();
            try{ e.sueldoBase=Double.parseDouble(tfS.getText().trim()); }catch(Exception ex){}
            try{ e.porcentajeComision=Double.parseDouble(tfCm.getText().trim()); }catch(Exception ex){}
            dao.guardar(e); d.dispose(); cargar();
        });
        d.add(p); d.setVisible(true);
    }

    private void comisiones() {
        VentaDAO vDao = new VentaDAO();
        StringBuilder sb = new StringBuilder("COMISIONES POR EMPLEADO\n\n");
        for(Empleado e : dao.listarActivos()){
            double total = vDao.listar().stream()
                .filter(v->v.idEmpleado.equals(e.id) && v.estado==Venta.Estado.CONFIRMADA)
                .mapToDouble(v->v.total).sum();
            double com = total * e.porcentajeComision / 100;
            sb.append(String.format("%-25s  ventas: %-12s  comisión: %s%n",
                e.nombre, Fmt.moneda(total), Fmt.moneda(com)));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(Colores.F_MONO); ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Comisiones", JOptionPane.INFORMATION_MESSAGE);
    }
}
