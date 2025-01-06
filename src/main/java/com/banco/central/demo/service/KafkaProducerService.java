package com.banco.central.demo.service;

import com.banco.central.demo.dto.MessageRequestDTO;


public interface KafkaProducerService {

    public String sendMessage(MessageRequestDTO message);

    public boolean deleteMessageById(String id);

}
