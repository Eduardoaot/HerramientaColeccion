package mk.coleccion.dto;

import java.util.List;

public class MangasResponseDTO {
    private String result;
    private List<PlanDTO> list;

    public MangasResponseDTO(String result, List<PlanDTO> list) {
        this.result = result;
        this.list = list;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<PlanDTO> getList() {
        return list;
    }

    public void setList(List<PlanDTO> list) {
        this.list = list;
    }
}

