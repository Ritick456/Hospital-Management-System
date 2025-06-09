package com.capgemini.hospital_management_system.controller;

import com.capgemini.hospital_management_system.dto.*;
import com.capgemini.hospital_management_system.exception.EntityNotFoundException;
import com.capgemini.hospital_management_system.model.Department;
import com.capgemini.hospital_management_system.model.Physician;
import com.capgemini.hospital_management_system.repository.DepartmentRepository;
import com.capgemini.hospital_management_system.repository.PhysicianRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PhysicianRepository physicianRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<Response<DepartmentDto>> createDepartment(@RequestBody CreateDepartmentDto createDepartmentDto) {

        Physician physician = physicianRepository.findById(createDepartmentDto.getPhysicianId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Physician not found with ID: " + createDepartmentDto.getPhysicianId()));

        Department department = new Department();

        department.setDepartmentId(createDepartmentDto.getDeptId());
        department.setName(createDepartmentDto.getName());
        department.setHead(physician);

        Department savedDepartment = departmentRepository.save(department);

        DepartmentDto responseDto = modelMapper.map(savedDepartment , DepartmentDto.class);

        PhysicianDepartmentDto physicianDto = modelMapper.map(physician, PhysicianDepartmentDto.class);
        responseDto.setPhysicianDetail(physicianDto);

        Response<DepartmentDto> response = Response.<DepartmentDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Department created successfully")
                .data(responseDto)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/")
    public ResponseEntity<Response<List<DepartmentDto>>> getAllDepartment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("departmentId").descending());
        Page<Department> departmentPage = departmentRepository.findAll(pageable);

        if (departmentPage.isEmpty()) {
            throw new EntityNotFoundException("No departments found.");
        }

        List<DepartmentDto> departmentDtoList = departmentPage.getContent().stream()
                .map(department -> {
                    DepartmentDto dto = modelMapper.map(department, DepartmentDto.class);
                    if (department.getHead() != null) {
                        dto.setPhysicianDetail(modelMapper.map(department.getHead(), PhysicianDepartmentDto.class));
                    }
                    return dto;
                }).collect(Collectors.toList());

        // Only send list — avoid sending Pageable/Sort metadata
        Response<List<DepartmentDto>> response = Response.<List<DepartmentDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Departments fetched successfully")
                .data(departmentDtoList)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FOUND);

    }





    @GetMapping("/{deptId}")
    public ResponseEntity<Response<DepartmentDto>> getDepartmentDetailsById(@PathVariable Integer deptId){

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new EntityNotFoundException("Department with id " + deptId + " not found"));

        Physician head = department.getHead();

        DepartmentDto departmentDto = modelMapper.map(department, DepartmentDto.class);
        PhysicianDepartmentDto physicianDepartmentDto = modelMapper.map(head , PhysicianDepartmentDto.class);

        departmentDto.setPhysicianDetail(physicianDepartmentDto);

        Response<DepartmentDto> response = Response.<DepartmentDto>builder()
                .status(HttpStatus.OK.value())
                .message("Department fetched successfully")
                .data(departmentDto)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response , HttpStatus.FOUND);

    }

    @GetMapping("/head/{deptId}")
    public ResponseEntity<Response<PhysicianDepartmentDto>> getDepartmentHeadDetailsById(@PathVariable Integer deptId){

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new EntityNotFoundException("Department with id " + deptId + " not found"));

        Physician head = department.getHead();

        PhysicianDepartmentDto physicianDepartmentDto = modelMapper.map(head , PhysicianDepartmentDto.class);

        Response<PhysicianDepartmentDto> response = Response.<PhysicianDepartmentDto>builder()
                .status(HttpStatus.OK.value())
                .message("Physician fetched successfully")
                .data(physicianDepartmentDto)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response , HttpStatus.FOUND);

    }

    @GetMapping("/deptHead/{headId}")
    public ResponseEntity<Response<List<DepartmentDto>>> getDepartmentsByHeadId(@PathVariable Integer headId) {

        List<Department> departments = departmentRepository.findAllByHeadEmployeeId(headId);

        if (departments.isEmpty()) {
            throw new EntityNotFoundException("No departments found for head ID " + headId);
        }

        List<DepartmentDto> departmentDtos = departments.stream()
                .map(dept -> {
                    DepartmentDto dto = modelMapper.map(dept, DepartmentDto.class);
                    PhysicianDepartmentDto physicianDto = modelMapper.map(dept.getHead(), PhysicianDepartmentDto.class);
                    dto.setPhysicianDetail(physicianDto);
                    return dto;
                })
                .toList();

        Response<List<DepartmentDto>> response = Response.<List<DepartmentDto>>builder()
                .status(HttpStatus.OK.value())
                .message("Departments fetched successfully for head ID " + headId)
                .data(departmentDtos)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check/{physicianId}")
    public ResponseEntity<Boolean> checkHeadRole(@PathVariable Integer physicianId) {
        boolean isHead = departmentRepository.existsByHeadEmployeeId(physicianId);
        return new ResponseEntity<>(isHead , HttpStatus.OK);
    }

    @PutMapping("/update/headid/{deptId}")
    public ResponseEntity<Response<DepartmentDto>> updateDepartmentHead(
            @PathVariable Integer deptId,
            @RequestBody UpdateDepartmentHeadRequest request) {

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + deptId));

        Physician newHead = physicianRepository.findById(request.getPhysicianId())
                .orElseThrow(() -> new EntityNotFoundException("Physician not found with id: " + request.getPhysicianId()));

        department.setHead(newHead);
        departmentRepository.save(department);

        DepartmentDto departmentDto = modelMapper.map(department, DepartmentDto.class);
        PhysicianDepartmentDto physicianDto = modelMapper.map(newHead, PhysicianDepartmentDto.class);

        departmentDto.setPhysicianDetail(physicianDto);

        Response<DepartmentDto> response = Response.<DepartmentDto>builder()
                .status(HttpStatus.OK.value())
                .message("Department head updated successfully")
                .data(departmentDto)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/deptname/{deptid}")
    public ResponseEntity<Response<DepartmentDto>> updateDepartmentName(
            @PathVariable Integer deptid,
            @RequestParam String newName) {

        Department department = departmentRepository.findById(deptid)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + deptid));

        department.setName(newName);
        departmentRepository.save(department);

        DepartmentDto departmentDto = modelMapper.map(department, DepartmentDto.class);
        PhysicianDepartmentDto physicianDepartmentDto = modelMapper.map(department.getHead() , PhysicianDepartmentDto.class);

        departmentDto.setPhysicianDetail(physicianDepartmentDto);

        Response<DepartmentDto> response = Response.<DepartmentDto>builder()
                .status(HttpStatus.OK.value())
                .message("Department name updated successfully")
                .data(departmentDto)
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
