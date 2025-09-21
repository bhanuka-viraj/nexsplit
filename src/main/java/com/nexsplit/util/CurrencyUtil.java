package com.nexsplit.util;

import com.nexsplit.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility class for currency-related operations.
 * 
 * This utility provides access to default currency configuration
 * and other currency-related operations.
 * 
 * @author NexSplit Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class CurrencyUtil {

    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        CurrencyUtil.appConfig = appConfig;
    }

    /**
     * Get the default currency from configuration.
     * 
     * @return The default currency code
     */
    public static String getDefaultCurrency() {
        return appConfig != null ? appConfig.getDefaultCurrency() : "USD";
    }
}
