package com.capgemini.hospital_management_system.controller;

import com.capgemini.hospital_management_system.dto.*;
import com.capgemini.hospital_management_system.model.*;
import com.capgemini.hospital_management_system.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PhysicianRepository physicianRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private DepartmentController departmentController;

    private Department department;
    private Physician physician;
    private DepartmentDto departmentDto;
    private PhysicianDepartmentDto physicianDto;

    @BeforeEach
    void setUp() {
        physician = new Physician();
        physician.setEmployeeId(101);
        physician.setName("Dr. House");

        department = new Department();
        department.setDepartmentId(1);
        department.setName("Cardiology");
        department.setHead(physician);

        departmentDto = new DepartmentDto();
        departmentDto.setDepartmentId(1);
        departmentDto.setName("Cardiology");

        physicianDto = new PhysicianDepartmentDto();
        physicianDto.setEmployeeId(101);
        physicianDto.setName("Dr. House");

        departmentDto.setPhysicianDetail(physicianDto);
    }

    @Test
    void testGetDepartmentDetailsById() {
        // Arrange
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(modelMapper.map(department, DepartmentDto.class)).thenReturn(departmentDto);
        when(modelMapper.map(physician, PhysicianDepartmentDto.class)).thenReturn(physicianDto);

        // Act
        ResponseEntity<Response<DepartmentDto>> response =
                departmentController.getDepartmentDetailsById(1);

        // Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        DepartmentDto responseDto = response.getBody().getData();
        assertEquals(1, responseDto.getDepartmentId());
        assertEquals("Cardiology", responseDto.getName());
        assertEquals("Dr. House", responseDto.getPhysicianDetail().getName());
        assertEquals(101, responseDto.getPhysicianDetail().getEmployeeId());
    }

    @Test
    void testGetDepartmentHeadDetailsById() {
        // Arrange
        when(departmentRepository.findById(10)).thenReturn(Optional.of(department));
        when(modelMapper.map(physician, PhysicianDepartmentDto.class)).thenReturn(physicianDto);

        // Act
        ResponseEntity<Response<PhysicianDepartmentDto>> response =
                departmentController.getDepartmentHeadDetailsById(10);

        // Assert
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        PhysicianDepartmentDto headDto = response.getBody().getData();
        assertEquals("Dr. House", headDto.getName());
        assertEquals(101, headDto.getEmployeeId());
    }

    @Test
    void testCreateDepartment_Success() {
        // Arrange
        CreateDepartmentDto createDto = new CreateDepartmentDto();
        createDto.setDeptId(1);
        createDto.setName("Cardiology");
        createDto.setPhysicianId(101);

        when(physicianRepository.findById(anyInt())).thenReturn(Optional.of(physician));
        when(departmentRepository.save(any(Department.class))).thenReturn(department);
        when(modelMapper.map(department, DepartmentDto.class)).thenReturn(departmentDto);
        when(modelMapper.map(physician, PhysicianDepartmentDto.class)).thenReturn(physicianDto);

        // Act
        ResponseEntity<Response<DepartmentDto>> response =
                departmentController.createDepartment(createDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Department created successfully", response.getBody().getMessage());

        DepartmentDto createdDept = response.getBody().getData();
        assertEquals(1, createdDept.getDepartmentId());
        assertEquals("Cardiology", createdDept.getName());
        assertEquals(101, createdDept.getPhysicianDetail().getEmployeeId());
    }


}