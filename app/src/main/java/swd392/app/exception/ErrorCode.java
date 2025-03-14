package swd392.app.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    USER_EXIST(1001, "User existed"),
    EMAIL_EXIST(1002, "Email existed"),
    USERNAME_INVALID(1003,"Username must be at least 3 characters"),
    EMAIL_INVALID(1004, "Email must be end with .@gmail.com"),
    PASSWORD_INVALID(1005, "Password must be at least 8 characters"),
    EMAIL_NOT_EXIST(1006, "User is not exist"),
    USER_NOT_EXIST(1007, "User not found"),
    UNAUTHENTICATED(1008, "Unauthenticated"),
    ROLE_NOT_FOUND(1009, "Role not found"),
    PRODUCT_CODE_EXIST(1010, "Product is exist"),
    PERMISSION_ERROR(1011, "You don't have permission to do this"),
    FULLNAME_REQUIRED(1012, "Fullname must not blank"),
    // Thêm mã lỗi mới cho chức năng kiểm kho
    WAREHOUSE_NOT_FOUND(1013, "Warehouse not found"),
    PRODUCT_NOT_FOUND(1014, "Product not found"),
    STOCK_CHECK_PRODUCTS_NOT_FOUND(1015, "Stock record not found for this product"),
    UNKNOWN_ERROR(1016, "Unknown error"),
    STOCK_CHECK_NOTE_NOT_FOUND(1017, "Stock check note not found"),
    USER_CODE_EXIST(1018, "User code is exist"),
    STOCK_CHECK_NOTE_CANNOT_BE_MODIFIED(1019, "Stock check note can not be modified"),
    STOCK_CHECK_NOTE_CANNOT_BE_FINALIZED(1020,"Stock check note can not be finalized"),
    PRODUCT_NOT_ENOUGH(1021, "Product is not enough to stock"),
    TRANSACTION_NOT_FOUND(1022, "Stock exchange note not found"),
    TRANSACTION_CANNOT_BE_MODIFIED(1023, "Stock exchange note can not be modified"),
    TRANSACTION_CANNOT_BE_FINALIZED(1024, "Stock exchange note can not be finalized"),
    INSUFFICIENT_STOCK(1025, "Not enough stock"),
    WAREHOUSE_REQUIRED(1026, "Warehouse is required"),
    NOTE_ITEMS_NOT_FOUND(1027, "Note item not found"),
    CAN_NOT_SYSTEM(1028, "SYSTEM only used for TRANSFER"),
    NOT_ENOUGH_QUANTITY(1029, "Not enough quantity"),
    INVALID_SOURCE_TYPE(1030,"Source type is incorrect"),
    INVALID_TRANSACTION_TYPE(1031, "Transaction type are: IMPORT, EXPORT, TRANSFER"),
    CATEGORY_CODE_EXIST(1032, "Category code is exist"),
    CATEGORY_NOT_FOUND(1033, "Category code is not found"),
    PRODUCT_TYPE_CODE_EXIST(1034, "Product type is exist"),
    PRODUCT_TYPE_NOT_FOUND(1035, "Product type is not found")
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

