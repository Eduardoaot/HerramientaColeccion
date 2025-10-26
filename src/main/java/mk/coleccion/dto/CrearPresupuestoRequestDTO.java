package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CrearPresupuestoRequestDTO {
    private String nombreBolsa;  // Nombre del presupuesto
    private Integer idUsuario;   // ID del usuario
    private Float descuento;     // Descuento
    private List<Integer> mangas_bolsa; // Lista de IDs de mangas a agregar

    public String getNombreBolsa() {
        return nombreBolsa;
    }

    public void setNombreBolsa(String nombreBolsa) {
        this.nombreBolsa = nombreBolsa;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Float getDescuento() {
        return descuento;
    }

    public void setDescuento(Float descuento) {
        this.descuento = descuento;
    }

    public List<Integer> getMangas_bolsa() {
        return mangas_bolsa;
    }

    public void setMangas_bolsa(List<Integer> mangas_bolsa) {
        this.mangas_bolsa = mangas_bolsa;
    }
}



