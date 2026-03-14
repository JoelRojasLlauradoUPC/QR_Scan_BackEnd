package edu.upc.dsa.event.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entrada")
@XmlAccessorType(XmlAccessType.FIELD)
public class Ticket {

    private String nombre;
    private String apellido;

    @XmlElement(name = "correo_electronico")
    private String correoElectronico;

    private int tipo;
    private boolean pmr;
    private String hash;

    @XmlElement(name = "numero_local")
    private int numeroLocal;

    private boolean consumed;

    public Ticket() {
    }

    public Ticket(String nombre, String apellido, String correoElectronico, int tipo, boolean pmr, String hash, int numeroLocal, boolean consumed) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correoElectronico = correoElectronico;
        this.tipo = tipo;
        this.pmr = pmr;
        this.hash = hash;
        this.numeroLocal = numeroLocal;
        this.consumed = consumed;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public boolean isPmr() {
        return pmr;
    }

    public void setPmr(boolean pmr) {
        this.pmr = pmr;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getNumeroLocal() {
        return numeroLocal;
    }

    public void setNumeroLocal(int numeroLocal) {
        this.numeroLocal = numeroLocal;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}

