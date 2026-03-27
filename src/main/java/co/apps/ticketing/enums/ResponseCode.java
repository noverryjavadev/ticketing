package co.apps.ticketing.enums;


public enum ResponseCode {

    SUCCESS("00", "success"),
    DATA_NOT_FOUND("40", "data not found");


    private String code;
    private String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }


}
