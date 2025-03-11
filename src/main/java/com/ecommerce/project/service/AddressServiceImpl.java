package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.AddressResponse;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        User user = authUtil.loggedInUser();
        Address addressFromDb = addressRepository.findByStreet(addressDTO.getStreet());
        if (addressFromDb != null)
            throw new APIException("Address with the name " + address.getStreet() + " already exists !!!");

        List<Address> userAddresses = user.getAddresses();
        userAddresses.add(address);
        user.setAddresses(userAddresses);
        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);

    }

    @Override
    public AddressResponse getAllAddresses(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Address> addressPage = addressRepository.findAll(pageDetails);

        List<Address> addresses = addressPage.getContent();
        if (addresses.isEmpty())
            throw new APIException("No address created till now.");

        List<AddressDTO> addressDTOS = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        AddressResponse addressResponse = new AddressResponse();
        addressResponse.setContent(addressDTOS);
        addressResponse.setPageNumber(addressPage.getNumber());
        addressResponse.setPageSize(addressPage.getSize());
        addressResponse.setTotalElements(addressPage.getTotalElements());
        addressResponse.setTotalPages(addressPage.getTotalPages());
        addressResponse.setLastPage(addressPage.isLast());
        return addressResponse;

    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(
                ()-> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressesByUser() {
        User user = authUtil.loggedInUser();
        List<Address> addresses = user.getAddresses();
        if(addresses == null){
            throw new ResourceNotFoundException("Address", "users", user.getUserName());
        }
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddress(AddressDTO addressDTO, Long addressId) {
        Address address = modelMapper.map(addressDTO, Address.class);
        Address addressFromDB = addressRepository.findById(addressId).orElseThrow(()->
                new ResourceNotFoundException("Address", "addressId", addressId));
        addressFromDB.setStreet(address.getStreet());
        addressFromDB.setBuildingName(address.getBuildingName());
        addressFromDB.setCity(address.getCity());
        addressFromDB.setState(address.getState());
        addressFromDB.setCountry(address.getCountry());
        addressFromDB.setPincode(address.getPincode());
        Address updateAddress = addressRepository.save(addressFromDB);
        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(oldAddress -> oldAddress.getAddressId().equals(addressId));
        user.getAddresses().add(updateAddress);
        userRepository.save(user);
        return modelMapper.map(updateAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO deleteAddress(Long addressId) {
        Address deleteAddress = addressRepository.findById(addressId).orElseThrow(()->
                new ResourceNotFoundException("Address", "addressId", addressId));
        User user = deleteAddress.getUser();
        user.getAddresses().removeIf(oldAddress -> oldAddress.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(deleteAddress);
        return modelMapper.map(deleteAddress, AddressDTO.class);
    }
}
