package com.estudo.schedulingService.repositories;

import com.estudo.schedulingService.entities.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicesRespository extends JpaRepository<Services, UUID>, JpaSpecificationExecutor<Services> {
    boolean existsByName(String name);
}
