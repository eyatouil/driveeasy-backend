package com.driveeasy.client.client;

/** Appels sortants de client-service vers location-service. */
public interface LocationServiceClient {
    /** @return true si le client a au moins une location RESERVEE ou EN_COURS. */
    boolean hasActiveLocation(Long clientId);
}
