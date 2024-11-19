package com.project.ssi_wypozyczalnia.config;

public class SecurityConfig {
    private static final String SECURITY_ENABLED_PROPERTY = "security.enabled";
    private static Boolean securityEnabled;

    // aby wylaczyc security nalezy edytowac konfiguracje uruchomieniowa
    // i dodac w vm options: -Dsecurity.enabled=false

    static {
        // Próba odczytu z właściwości systemowej
        String securityEnabledProp = System.getProperty(SECURITY_ENABLED_PROPERTY);
        if (securityEnabledProp == null) {
            // Jeśli nie znaleziono właściwości systemowej, sprawdź zmienną środowiskową
            securityEnabledProp = System.getenv("SECURITY_ENABLED");
        }
        // Domyślnie security włączone
        securityEnabled = securityEnabledProp == null || Boolean.parseBoolean(securityEnabledProp);
    }

    public static boolean isSecurityEnabled() {
        return securityEnabled;
    }
} 