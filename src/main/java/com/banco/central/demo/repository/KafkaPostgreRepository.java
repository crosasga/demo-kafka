package com.banco.central.demo.repository;

import com.banco.central.demo.repository.model.KafkaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KafkaPostgreRepository extends JpaRepository<KafkaModel, String> {
}
