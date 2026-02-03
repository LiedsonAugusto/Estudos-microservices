package com.estudo.schedulingService.services;

import com.estudo.schedulingService.dtos.CreateServiceRequest;
import com.estudo.schedulingService.dtos.PageResponse;
import com.estudo.schedulingService.dtos.ServiceResponse;
import com.estudo.schedulingService.entities.Services;
import com.estudo.schedulingService.exceptions.ServiceAlreadyExistsException;
import com.estudo.schedulingService.exceptions.ServiceNotFoundException;
import com.estudo.schedulingService.repositories.ServicesRespository;
import com.estudo.schedulingService.specifications.ServicesSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ServicesService {

    private final ServicesRespository serviceRespository;

    public ServicesService(ServicesRespository serviceRespository) {
        this.serviceRespository = serviceRespository;
    }

    @Transactional
    public ServiceResponse create(CreateServiceRequest serviceDTO) {

        if (serviceRespository.existsByName(serviceDTO.name())) {
            throw new ServiceAlreadyExistsException(serviceDTO.name());
        }

        Services serviceCreate = new Services();
        serviceCreate.setName(serviceDTO.name());
        serviceCreate.setDescription(serviceDTO.description());
        serviceCreate.setDurationMinutes(serviceDTO.durationMinutes());
        serviceCreate.setCreatedAt(LocalDateTime.now());
        Services service = serviceRespository.save(serviceCreate);
        return mapToServiceResponse(service);
    }

    public List<ServiceResponse> getAllServices() {
        return serviceRespository.findAll()
                .stream()
                .map(this::mapToServiceResponse)
                .toList();
    }

    public PageResponse<ServiceResponse> getAllServicesPaginated(Pageable pageable) {

        Page<Services> servicePage = serviceRespository.findAll(pageable);

        List<ServiceResponse> listServices = servicePage.getContent()
                .stream()
                .map(this::mapToServiceResponse)
                .toList();

        return new PageResponse<>(
                listServices,
                servicePage.getNumber(),
                servicePage.getTotalPages(),
                servicePage.getTotalElements(),
                servicePage.getSize(),
                servicePage.hasNext(),
                servicePage.hasPrevious(),
                servicePage.isFirst(),
                servicePage.isLast()
        );
    }

    public PageResponse<ServiceResponse> searchServices(String name, String description,
                                                        Integer durationMinutes, Boolean active,
                                                        Pageable pageable) {
        Specification<Services> specification = ServicesSpecification.withFilters(
                name, description, durationMinutes, active
        );
        Page<Services> servicesPage = serviceRespository.findAll(specification, pageable);

        List<ServiceResponse> services = servicesPage.getContent()
                .stream()
                .map(this::mapToServiceResponse)
                .toList();

        return new PageResponse<>(
                services,
                servicesPage.getNumber(),
                servicesPage.getTotalPages(),
                servicesPage.getTotalElements(),
                servicesPage.getSize(),
                servicesPage.hasNext(),
                servicesPage.hasPrevious(),
                servicesPage.isFirst(),
                servicesPage.isLast()
        );
    }

    public ServiceResponse getServiceById(UUID id) {
        Services service = serviceRespository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado"));
        return mapToServiceResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(UUID id, CreateServiceRequest serviceDTO) {
        Services service = serviceRespository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado"));

        if (!service.getName().equals(serviceDTO.name()) &&
                serviceRespository.existsByName(serviceDTO.name())) {
            throw new ServiceAlreadyExistsException(serviceDTO.name());
        }

        service.setName(serviceDTO.name());
        service.setDescription(serviceDTO.description());
        service.setDurationMinutes(serviceDTO.durationMinutes());

        Services updatedService = serviceRespository.save(service);
        return mapToServiceResponse(updatedService);
    }

    @Transactional
    public void deleteService(UUID id) {
        Services service = serviceRespository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Serviço não encontrado"));
        service.setActive(false);
        serviceRespository.save(service);
    }

    public ServiceResponse mapToServiceResponse(Services service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.isActive()
        );
    }
}
