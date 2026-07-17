package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.core.dto.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dtos.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
        name = "Productos", description = "Operaciones relacionadas con la gestión de productos"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @Operation(summary = "Listar productos", description = "Obtiene la lista completa de productos activos.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping
    public List<ProductResponseDto> findAll() { return service.findAll(); }

    @Operation(summary = "Obtener producto por ID", description = "Devuelve un producto según su identificador.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping("/{id}")
    public ProductResponseDto findOne(@PathVariable Long id) { return service.findOne(id); }

    @Operation(summary = "Crear producto", description = "Registra un nuevo producto en el sistema.")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(
            @Valid @RequestBody CreateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.create(dto, currentUser);
    }

    @Operation(summary = "Actualizar producto", description = "Reemplaza completamente los datos de un producto existente.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.update(id, dto, currentUser);
    }

    @Operation(summary = "Actualización parcial de producto", description = "Modifica uno o más campos de un producto sin reemplazar los demás.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado parcialmente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @PatchMapping("/{id}")
    public ProductResponseDto partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PartialUpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.partialUpdate(id, dto, currentUser);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina de forma lógica un producto del sistema.")
    @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        service.delete(id, currentUser);
    }

    @Operation(summary = "Listar productos paginados", description = "Devuelve productos en páginas de tamaño configurable.")
    @ApiResponse(responseCode = "200", description = "Página obtenida exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping("/page")
    public Page<ProductResponseDto> findAllPage(@Valid @ModelAttribute PaginationDto pagination) {
        return service.findAllPage(pagination);
    }

    @Operation(summary = "Listar productos con slice", description = "Devuelve un slice de productos sin calcular el total de páginas.")
    @ApiResponse(responseCode = "200", description = "Slice obtenido exitosamente")
    @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @GetMapping("/slice")
    public Slice<ProductResponseDto> findAllSlice(@Valid @ModelAttribute PaginationDto pagination) {
        return service.findAllSlice(pagination);
    }
}