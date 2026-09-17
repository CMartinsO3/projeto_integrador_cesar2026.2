package com.hemoflow.hemoflow.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hemoflow.hemoflow.dominio.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
