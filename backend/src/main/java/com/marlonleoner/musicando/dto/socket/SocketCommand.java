package com.marlonleoner.musicando.dto.socket;

import tools.jackson.databind.JsonNode;

/**
 * Envelope dos comandos enviados pelos clientes em {@code /musicando/command}.
 *
 * @param type    tipo do comando (ver {@code SocketCommandType})
 * @param payload dados do comando, quando houver
 */
public record SocketCommand(String type, JsonNode payload) {
}
