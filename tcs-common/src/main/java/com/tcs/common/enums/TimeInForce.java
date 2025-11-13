package com.tcs.common.enums;

/**
 * Time in force enumeration
 */
public enum TimeInForce {
    GTC,  // Good Till Cancelled - remains active until filled or cancelled
    IOC,  // Immediate or Cancel - execute immediately, cancel unfilled portion
    FOK   // Fill or Kill - execute entire order immediately or cancel
}
