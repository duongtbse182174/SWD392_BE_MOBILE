package swd392.app.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStockRequest {
    @NotBlank(message = "Product code cannot be empty")
    String productCode;

    @NotBlank(message = "Warehouse code cannot be empty")
    String warehouseCode;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity;
}
