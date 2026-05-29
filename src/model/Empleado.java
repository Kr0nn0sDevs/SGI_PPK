package model;

public class Empleado {
    public String id, nombre, curp, puesto, correo, fechaIngreso;
    public double sueldoBase, porcentajeComision;
    public boolean activo = true;
    @Override public String toString() { return nombre + " (" + puesto + ")"; }
}
