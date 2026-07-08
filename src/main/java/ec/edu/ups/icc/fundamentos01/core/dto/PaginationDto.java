package ec.edu.ups.icc.fundamentos01.core.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PaginationDto {

    @Min(value = 0, message = "La pagina debe ser mayor o igual a 0")
    private int page = 0;

    @Min(value = 1, message = "El tamano debe ser mayor o igual a 1")
    @Max(value = 100, message = "El tamano no debe superar 100 registros")
    private int size = 10;

    private String sortBy = "id";

    private String direction = "asc";

    public PaginationDto() {}

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
}
