package ru.kromarong.common;

public enum DataType {
    EMPTY((byte) -1), FILE((byte) 15), COMMAND((byte) 16);

    byte firstMessageByte;

    DataType(byte firstMessageByte) {
        this.firstMessageByte = firstMessageByte;
    }

    public static DataType getDataTypeFromByte(byte b) {
        if (b == FILE.firstMessageByte) {
            return FILE;
        }
        if (b == COMMAND.firstMessageByte) {
            return COMMAND;
        }
        return EMPTY;
    }
}
