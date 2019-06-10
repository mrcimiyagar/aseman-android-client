package com.microsoft.signalr.interfaces;

import com.microsoft.signalr.JsonConverterType;

public interface ConfigFetchigController {
    boolean isLoggerEnabled();
    JsonConverterType jsonConverterType();
}
