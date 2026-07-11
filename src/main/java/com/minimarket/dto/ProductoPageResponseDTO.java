package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import com.minimarket.entity.Categoria;

@Schema(description = "DTO para la respuesta paginada de productos en formato HATEOAS")
public class ProductoPageResponseDTO {

    @JsonProperty("_embedded")
    @Schema(description = "Contenedor de la lista de productos")
    private Embedded embedded;

    @JsonProperty("_links")
    @Schema(description = "Enlaces de navegación de la paginación")
    private Map<String, Link> links;

    @Schema(description = "Metadatos de la página actual")
    private PageMetadata page;

    public Embedded getEmbedded() { return embedded; }
    public void setEmbedded(Embedded embedded) { this.embedded = embedded; }

    public Map<String, Link> getLinks() { return links; }
    public void setLinks(Map<String, Link> links) { this.links = links; }

    public PageMetadata getPage() { return page; }
    public void setPage(PageMetadata page) { this.page = page; }

    public static class Embedded {
        private java.util.List<ProductoResponseDTO> productoList;
        public java.util.List<ProductoResponseDTO> getProductoList() { return productoList; }
        public void setProductoList(java.util.List<ProductoResponseDTO> productoList) { this.productoList = productoList; }
    }

    public static class Link {
        private String href;
        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }
    }

    public static class PageMetadata {
        private int size;
        private long totalElements;
        private int totalPages;
        private int number;

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
    }
}
