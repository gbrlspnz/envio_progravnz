package equipo2.logica;

import equipo2.modelo.Envio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestor del catálogo de envíos en memoria.
 * Administra la colección polimórfica List<Envio> con objetos de ambas clases hijas.
 * Garantiza la unicidad de los códigos sin distinguir mayúsculas/minúsculas y quitando espacios extremos.
 */
public class GestorEnvios {

    private final List<Envio> envios = new ArrayList<>();

    public List<Envio> getEnvios() {
        return Collections.unmodifiableList(envios);
    }

    public List<Envio> getEnviosModificables() {
        return envios;
    }

    public void setEnvios(List<Envio> nuevosEnvios) {
        this.envios.clear();
        if (nuevosEnvios != null) {
            this.envios.addAll(nuevosEnvios);
        }
    }

    public boolean existeCodigo(String codigo) {
        if (codigo == null) return false;
        String normalizado = codigo.trim();
        for (Envio e : envios) {
            if (e.getCodigo().trim().equalsIgnoreCase(normalizado)) {
                return true;
            }
        }
        return false;
    }

    public void agregarEnvio(Envio envio) {
        if (envio == null) {
            throw new IllegalArgumentException("El envío no puede ser nulo.");
        }
        if (existeCodigo(envio.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un envío registrado con el código: " + envio.getCodigo());
        }
        this.envios.add(envio);
    }

    public Envio eliminarEnvio(int indice) {
        if (indice < 0 || indice >= envios.size()) {
            throw new IndexOutOfBoundsException("Índice de envío inválido: " + indice);
        }
        return this.envios.remove(indice);
    }

    public int getCantidad() {
        return envios.size();
    }

    public void limpiar() {
        this.envios.clear();
    }
}
