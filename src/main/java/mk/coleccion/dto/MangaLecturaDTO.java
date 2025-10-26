package mk.coleccion.dto;

import java.util.List;

public class MangaLecturaDTO {
    private long totalMangasLeidos;
    private long totalMangaLeidosMes;
    private long totalMangaLeidosAnio;
    private List<MangaSinLeerDTO> listaMangasSinLeer;
    private List<Integer> listaMangasCompradosData;
    private List<Integer> listaMangasLeidosData;
    private List<String> listaMangasMeses;

    public long getTotalMangasLeidos() {
        return totalMangasLeidos;
    }

    public void setTotalMangasLeidos(long totalMangasLeidos) {
        this.totalMangasLeidos = totalMangasLeidos;
    }

    public long getTotalMangaLeidosMes() {
        return totalMangaLeidosMes;
    }

    public void setTotalMangaLeidosMes(long totalMangaLeidosMes) {
        this.totalMangaLeidosMes = totalMangaLeidosMes;
    }

    public long getTotalMangaLeidosAnio() {
        return totalMangaLeidosAnio;
    }

    public void setTotalMangaLeidosAnio(long totalMangaLeidosAnio) {
        this.totalMangaLeidosAnio = totalMangaLeidosAnio;
    }

    public List<MangaSinLeerDTO> getListaMangasSinLeer() {
        return listaMangasSinLeer;
    }

    public void setListaMangasSinLeer(List<MangaSinLeerDTO> listaMangasSinLeer) {this.listaMangasSinLeer = listaMangasSinLeer;}

    public List<Integer> getListaMangasCompradosData() {
        return listaMangasCompradosData;
    }

    public void setListaMangasCompradosData(List<Integer> listaMangasCompradosData) {this.listaMangasCompradosData = listaMangasCompradosData;}

    public List<Integer> getListaMangasLeidosData() {
        return listaMangasLeidosData;
    }

    public void setListaMangasLeidosData(List<Integer> listaMangasLeidosData) {this.listaMangasLeidosData = listaMangasLeidosData;}

    public List<String> getListaMangasMeses() {
        return listaMangasMeses;
    }

    public void setListaMangasMeses(List<String> listaMangasMeses) {
        this.listaMangasMeses = listaMangasMeses;
    }
}