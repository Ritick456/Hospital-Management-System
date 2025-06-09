package com.capgemini.hospital_management_system.controller;


import com.capgemini.hospital_management_system.dto.DepartmentDto;
import com.capgemini.hospital_management_system.dto.PhysicianDepartmentDto;
import com.capgemini.hospital_management_system.dto.Response;
import com.capgemini.hospital_management_system.model.AffiliatedWith;
import com.capgemini.hospital_management_system.model.Physician;
import com.capgemini.hospital_management_system.repository.AffiliatedWithRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/affiliated_with")
public class AffiliatedWithController {

    @Autowired
    private AffiliatedWithRepository affiliatedWithRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/physicians/{deptId}")
    public ResponseEntity<Response<List<PhysicianDepartmentDto>>> getPhysiciansByDepartment(@PathVariable Integer deptId) {
        List<AffiliatedWith> affiliations = affiliatedWithRepository.findByDepartment_DepartmentId(deptId);

        List<PhysicianDepartmentDto> physicianDtos = affiliations.stream()
                .map(aff -> modelMapper.map(aff.getPhysician(), PhysicianDepartmentDto.class))
                .toList();

        Response<List<PhysicianDepartmentDto>> response = Response.<List<PhysicianDepartmentDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Physicians affiliated with department ID: " + deptId)
                .data(physicianDtos)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response , HttpStatus.FOUND);

    }


    @GetMapping("/department/{physicianId}")
    public ResponseEntity<Response<List<DepartmentDto>>> getDepartmentsByPhysician(@PathVariable Long physicianId) {
        List<AffiliatedWith> affiliations = affiliatedWithRepository.findByPhysician_EmployeeId(physicianId);

        List<DepartmentDto> departments = affiliations.stream()
                .map(aff -> {

                    DepartmentDto dto = modelMapper.map(aff.getDepartment(), DepartmentDto.class);

                    PhysicianDepartmentDto physicianDto = modelMapper.map(aff.getPhysician(), PhysicianDepartmentDto.class);
                    dto.setPhysicianDetail(physicianDto);

                    return dto;
                })
                .toList();

        Response<List<DepartmentDto>> response = Response.<List<DepartmentDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Departments affiliated with physician ID: " + physicianId)
                .data(departments)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response , HttpStatus.FOUND);
    }


    @GetMapping("/count-physicians/{deptId}")
        public ResponseEntity<Response<Integer>> countPhysiciansByDepartment(@PathVariable Integer deptId) {
            int count = affiliatedWithRepository.findByDepartment_DepartmentId(deptId).size();

            Response<Integer> response = Response.<Integer>builder()
                    .status(HttpStatus.OK.value())
                    .message("Physician count for department ID: " + deptId)
                    .data(count)
                    .time(LocalDateTime.now())
                    .build();

            return new ResponseEntity<>(response , HttpStatus.FOUND);
        }


}
