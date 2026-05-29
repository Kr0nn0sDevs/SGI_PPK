package dao;

import model.Usuario;
import util.Json;
import util.Json.JArray;
import util.Json.JObj;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private static final String F = "data/usuarios.json";

    public List<Usuario> listar() {
        List<Usuario> l = new ArrayList<>();
        for (Object o : Json.readArray(F)) l.add(from((JObj)o));
        return l;
    }

    public Usuario porUsuario(String u) {
        return listar().stream().filter(x -> x.usuario.equals(u)).findFirst().orElse(null);
    }

    public void guardar(Usuario u) {
        JArray arr = Json.readArray(F);
        arr.removeIf(o -> ((JObj)o).getStr("id").equals(u.id));
        arr.add(to(u));
        Json.writeArray(F, arr);
    }

    private JObj to(Usuario u) {
        JObj o = new JObj();
        o.put("id", u.id); o.put("nombre", u.nombre);
        o.put("usuario", u.usuario); o.put("password", u.password);
        o.put("rol", u.rol.name()); o.put("activo", u.activo);
        o.put("intentosFallidos", u.intentosFallidos);
        return o;
    }

    private Usuario from(JObj o) {
        Usuario u = new Usuario();
        u.id = o.getStr("id"); u.nombre = o.getStr("nombre");
        u.usuario = o.getStr("usuario"); u.password = o.getStr("password");
        u.rol = Usuario.Rol.valueOf(o.getStr("rol"));
        u.activo = o.getBool("activo");
        u.intentosFallidos = o.getInt("intentosFallidos");
        return u;
    }
}
