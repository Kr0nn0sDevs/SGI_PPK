
package util;


import model.Usuario;

public class Session {
    private static Session inst;
    private Usuario usuario;
    private Session() {}
    public static Session get() {
        if (inst == null) inst = new Session();
        return inst;
    }
    public void login(Usuario u)  { this.usuario = u; }
    public void logout()          { this.usuario = null; }
    public Usuario usuario()      { return usuario; }
    public boolean isAdmin()      { return usuario != null && usuario.rol == Usuario.Rol.ADMIN; }
    public String nombre()        { return usuario != null ? usuario.nombre : ""; }
    public String idUsuario()     { return usuario != null ? usuario.id : ""; }
}
