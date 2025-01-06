package com.banco.central.demo.service;

import com.banco.central.demo.dto.MessageRequestDTO;
import java.util.List;

public interface KafkaConsumerService {

    public List<MessageRequestDTO> listAllMessages();

    public MessageRequestDTO buscarMensajePorKey(String key);
}
