package model;

public class Usuario {
    public enum Rol { ADMIN, VENDEDOR }
    public String id, nombre, usuario, password;
    public Rol rol;
    public boolean activo = true;
    public int intentosFallidos = 0;
}
